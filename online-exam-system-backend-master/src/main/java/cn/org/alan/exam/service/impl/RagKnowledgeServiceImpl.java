package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.RagKnowledgeMapper;
import cn.org.alan.exam.model.entity.RagKnowledge;
import cn.org.alan.exam.model.entity.StudyMaterial;
import cn.org.alan.exam.service.IRagKnowledgeService;
import cn.org.alan.exam.service.IRagParseTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.ImageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class RagKnowledgeServiceImpl extends ServiceImpl<RagKnowledgeMapper, RagKnowledge> implements IRagKnowledgeService {

    // Tesseract 安装路径（根据你的实际路径修改）
    private static final String TESSERACT_PATH = "C:\\Program Files\\Tesseract-OCR\\tesseract.exe";
    
    // 线程池用于并行处理
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
    );

    @Resource
    private IRagParseTaskService ragParseTaskService;

    // ========================== 解析与存储 ==========================
    @Override
    @Transactional
    public Result<String> parseAndStoreMaterial(StudyMaterial studyMaterial) {
        return parseAndStoreMaterialWithProgress(studyMaterial, null);
    }
    
    @Override
    @Transactional
    public Result<String> parseAndStoreMaterialWithProgress(StudyMaterial studyMaterial, Integer taskId) {
        log.info("开始解析资料 - ID: {}, 文件名: {}, 类型: {}", 
                studyMaterial.getId(), studyMaterial.getOriginalName(), studyMaterial.getFileType());
        
        try {
            // 验证文件
            File file = new File(studyMaterial.getFilePath());
            if (!file.exists()) {
                log.error("文件不存在: {}", studyMaterial.getFilePath());
                if (taskId != null) {
                    ragParseTaskService.markFailed(taskId, "文件不存在");
                }
                return Result.failed("文件不存在，请重新上传文件");
            }
            
            if (!file.canRead()) {
                log.error("文件不可读: {}", studyMaterial.getFilePath());
                if (taskId != null) {
                    ragParseTaskService.markFailed(taskId, "文件不可读");
                }
                return Result.failed("文件不可读，请检查文件权限");
            }
            
            long fileSize = file.length();
            if (fileSize == 0) {
                log.error("文件为空: {}", studyMaterial.getOriginalName());
                if (taskId != null) {
                    ragParseTaskService.markFailed(taskId, "文件为空");
                }
                return Result.failed("文件内容为空，请上传有效的文件");
            }
            
            log.info("文件验证通过 - 大小: {} bytes ({} MB)", fileSize, String.format("%.2f", fileSize / 1024.0 / 1024.0));
            
            // 提取文本
            log.info("开始提取文本...");
            if (taskId != null) {
                ragParseTaskService.updateProgress(taskId, 5, 0);
            }
            
            String content = extractTextByFileType(file, studyMaterial.getFileType(), taskId);
            
            if (content == null || content.trim().isEmpty()) {
                log.warn("文本提取失败或为空 - 文件: {}, 类型: {}", studyMaterial.getOriginalName(), studyMaterial.getFileType());
                if (taskId != null) {
                    ragParseTaskService.markFailed(taskId, "文本提取失败");
                }
                return Result.failed("文件无法识别文本内容，请确认文件格式是否正确或PDF/图像质量是否足够清晰");
            }

            log.info("成功提取文本长度: {} 字符", content.length());

            // 分块处理
            log.info("开始分块处理...");
            if (taskId != null) {
                ragParseTaskService.updateProgress(taskId, 50, 0);
            }
            
            List<String> chunks = splitText(content, 800, 200);
            
            if (chunks.isEmpty()) {
                log.warn("文本分块为空");
                if (taskId != null) {
                    ragParseTaskService.markFailed(taskId, "文本分块失败");
                }
                return Result.failed("文本处理失败，无法生成知识库块");
            }
            
            log.info("文本分块完成，共 {} 块", chunks.size());
            
            // 删除旧的知识库数据
            log.info("删除旧的知识库数据...");
            int deletedCount = baseMapper.deleteByStudyMaterialId(studyMaterial.getId());
            log.info("删除旧知识库数据: {} 条", deletedCount);

            // 保存新的知识库数据
            log.info("开始保存知识库数据...");
            for (int i = 0; i < chunks.size(); i++) {
                RagKnowledge knowledge = new RagKnowledge();
                knowledge.setUserId(studyMaterial.getUserId());
                knowledge.setStudyMaterialId(studyMaterial.getId());
                knowledge.setChunkContent(chunks.get(i));
                knowledge.setChunkIndex(i);
                save(knowledge);
                
                // 更新进度
                if (taskId != null && (i + 1) % 10 == 0) {
                    int progress = 50 + (int)((i + 1) * 50.0 / chunks.size());
                    ragParseTaskService.updateProgress(taskId, progress, 0);
                }
                
                if ((i + 1) % 10 == 0 || i == chunks.size() - 1) {
                    log.info("保存进度: {}/{}", i + 1, chunks.size());
                }
            }
            
            log.info("解析完成 - 总块数: {}", chunks.size());
            
            // 标记任务完成
            if (taskId != null) {
                ragParseTaskService.markCompleted(taskId, chunks.size());
            }
            
            return Result.success("解析成功，共" + chunks.size() + "块知识内容");
            
        } catch (OutOfMemoryError e) {
            log.error("内存不足异常", e);
            if (taskId != null) {
                ragParseTaskService.markFailed(taskId, "内存不足");
            }
            return Result.failed("文件过大导致内存不足，请上传较小的文件或联系管理员增加服务器内存");
        } catch (Exception e) {
            log.error("解析资料异常 - 文件: {}, 错误类型: {}, 错误信息: {}", 
                     studyMaterial.getOriginalName(), 
                     e.getClass().getSimpleName(),
                     e.getMessage(), 
                     e);
            
            if (taskId != null) {
                ragParseTaskService.markFailed(taskId, e.getMessage());
            }
            
            // 提供更详细的错误信息
            String errorMsg;
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Network Error") || e.getMessage().contains("Connection")) {
                    errorMsg = "网络连接错误，请检查网络连接后重试";
                } else if (e.getMessage().contains("Timeout") || e.getMessage().contains("timed out")) {
                    errorMsg = "请求超时，请稍后重试。如果文件较大，请耐心等待";
                } else if (e.getMessage().contains("Tesseract") || e.getMessage().contains("OCR")) {
                    errorMsg = "OCR识别失败，请确认Tesseract-OCR已正确安装到：C:\\Program Files\\Tesseract-OCR";
                } else if (e.getMessage().contains("PDF") || e.getMessage().contains("pdfbox")) {
                    errorMsg = "PDF文件处理失败：" + e.getMessage();
                } else if (e.getMessage().contains("AccessDenied") || e.getMessage().contains("权限")) {
                    errorMsg = "文件访问权限不足，请检查文件权限设置";
                } else if (e.getMessage().contains("IOException") || e.getMessage().contains("IO error")) {
                    errorMsg = "文件读写错误：" + e.getMessage();
                } else {
                    errorMsg = "解析失败：" + e.getMessage();
                }
            } else {
                errorMsg = "解析失败：未知错误，请查看后端日志获取详细信息";
            }
            
            return Result.failed(errorMsg);
        }
    }

    private String extractTextByFileType(File file, String fileType, Integer taskId) {
        try {
            String type = fileType.toLowerCase().trim();
            log.debug("开始提取文本，文件类型：{}，文件：{}", type, file.getName());
            
            String result = "";
            switch (type) {
                case "pdf":
                    result = extractPdf(file, taskId);
                    break;
                case "txt":
                    result = extractTxt(file);
                    if (taskId != null) {
                        ragParseTaskService.updateProgress(taskId, 45, 0);
                    }
                    break;
                default:
                    log.error("不支持的文件类型：{}", fileType);
                    return "";
            }
            
            log.debug("文本提取完成，类型：{}，字符数：{}", type, result == null ? 0 : result.length());
            return result;
            
        } catch (Exception e) {
            log.error("提取文本失败，文件：{}，类型：{}", file.getName(), fileType, e);
            return "";
        }
    }

    private String extractTextByFileType(File file, String fileType) {
        return extractTextByFileType(file, fileType, null);
    }

    private String extractPdf(File file, Integer taskId) throws IOException {
        // 先尝试直接提取文字层
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            // 提高阈值，让更多PDF进入OCR（扫描版PDF文字层可能很少）
            if (text != null && text.trim().length() > 200) {
                log.info("PDF 文字型识别成功，字符数：{}", text.length());
                if (taskId != null) {
                    ragParseTaskService.updateProgress(taskId, 45, 0);
                }
                return text;   // 文字型 PDF
            }
            log.info("PDF 文字层为空或过少（只有{}字符），转向OCR处理", text == null ? 0 : text.length());
        } catch (Exception e) {
            log.warn("PDFBox提取文本失败，转向OCR处理：{}", e.getMessage());
        }
        // 文字层为空，可能是扫描版，调用 OCR
        return ocrPdfWithTesseract(file, taskId);
    }

    private String extractPdf(File file) throws IOException {
        return extractPdf(file, null);
    }

    private String extractTxt(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    /**
     * OCR 扫描版 PDF（优化版：图像预处理 + 并行页面处理 + 带进度跟踪）
     */
    private String ocrPdfWithTesseract(File pdfFile, Integer taskId) {
        StringBuilder result = new StringBuilder();
        File tempDir = null;
        int successCount = 0, failCount = 0;
        
        try {
            // 检查 tesseract 是否存在
            File tesseract = new File(TESSERACT_PATH);
            if (!tesseract.exists()) {
                String errorMsg = "Tesseract-OCR未安装或路径不正确，请确认Tesseract已正确安装到：" + TESSERACT_PATH;
                log.warn(errorMsg);
                log.warn("将尝试使用文字层提取，如果PDF没有文字层则无法解析");
                if (taskId != null) {
                    // 不立即标记失败，让上游继续处理
                }
                // Tesseract不存在时返回空，让上游代码处理
                return "";
            }
            
            log.info("Tesseract 路径验证通过: {}", TESSERACT_PATH);
            
            tempDir = Files.createTempDirectory("pdf_ocr_").toFile();
            log.info("OCR 临时目录: {}", tempDir.getAbsolutePath());

            try (PDDocument doc = PDDocument.load(pdfFile)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                int pages = doc.getNumberOfPages();
                log.info("PDF OCR 开始处理，共 {} 页", pages);
                
                // 更新总页数
                if (taskId != null) {
                    ragParseTaskService.updateProgress(taskId, 5, 0);
                }

                // 根据页数选择合适的DPI，页数多则降低DPI以提高速度
                int dpi = pages > 30 ? 150 : pages > 15 ? 200 : 250;
                log.info("使用 {} DPI 进行OCR渲染", dpi);

                // 创建final变量供lambda表达式使用
                final PDFRenderer finalRenderer = renderer;
                final int finalPages = pages;
                final int finalDpi = dpi;
                final File finalTempDir = tempDir;

                // 并行处理：使用线程池同时处理多个页面，大幅提升多页PDF处理速度
                // 每个任务返回 [页码, OCR文本]，按页码排序后拼接
                List<Future<Object[]>> futures = new ArrayList<>();
                int parallelism = Math.min(pages, Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
                log.info("并行处理线程数: {}", parallelism);

                for (int page = 0; page < pages; page++) {
                    final int pageNum = page;
                    futures.add(EXECUTOR_SERVICE.submit(() -> {
                        String pageText = processSinglePage(finalRenderer, pageNum, finalPages, finalDpi, finalTempDir);
                        return new Object[]{pageNum, pageText};
                    }));
                }

                // 收集结果并更新进度
                for (Future<Object[]> future : futures) {
                    try {
                        Object[] data = future.get();
                        int pageNum = (int) data[0];
                        String pageText = (String) data[1];
                        
                        if (pageText != null && !pageText.trim().isEmpty()) {
                            result.append(pageText.trim()).append("\n");
                            successCount++;
                        } else {
                            failCount++;
                        }

                        // 更新进度
                        if (taskId != null) {
                            int completed = successCount + failCount;
                            int progress = 5 + (int)(completed * 40.0 / pages);
                            ragParseTaskService.updateProgress(taskId, progress, completed);
                        }
                    } catch (Exception e) {
                        log.warn("获取页面处理结果异常: {}", e.getMessage());
                        failCount++;
                    }
                }
            }
            
            log.info("PDF OCR 处理统计 - 总页数: {}, 成功: {}, 失败: {}", 
                    successCount + failCount, successCount, failCount);
            
        } catch (Exception e) {
            log.error("OCR 处理异常", e);
            // 即使OCR失败，也不抛出异常，让系统继续尝试处理
            if (taskId != null) {
                // 不立即标记失败，继续尝试其他处理方式
            }
        } finally {
            if (tempDir != null) {
                try {
                    Files.walk(tempDir.toPath())
                            .sorted(Comparator.reverseOrder())
                            .map(java.nio.file.Path::toFile)
                            .forEach(f -> {
                                if (!f.delete()) {
                                    log.warn("删除临时文件失败: {}", f.getAbsolutePath());
                                }
                            });
                    log.debug("临时目录清理完成: {}", tempDir.getAbsolutePath());
                } catch (IOException e) {
                    log.warn("清理临时目录失败: {}", e.getMessage());
                }
            }
        }
        
        String finalResult = result.toString().trim();
        if (finalResult.isEmpty()) {
            log.warn("PDF OCR 最终结果为空，可能原因: 1.所有页面都无法识别 2.PDF质量太差 3.Tesseract配置问题");
        } else {
            log.info("PDF OCR 最终提取 {} 字符", finalResult.length());
        }
        return finalResult;
    }

    /**
     * 处理单个PDF页面：渲染 → 图像预处理（灰度+对比度增强+Otsu二值化+中值滤波） → OCR识别
     */
    private String processSinglePage(PDFRenderer renderer, int page, int totalPages, int dpi, File tempDir) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("正在处理第 {}/{} 页...", page + 1, totalPages);
            
            BufferedImage image = null;
            
            // 渲染PDF页面为图像
            try {
                image = renderer.renderImageWithDPI(page, dpi, ImageType.RGB);
                log.debug("第 {} 页使用 {}DPI 渲染成功", page, dpi);
            } catch (Exception e1) {
                log.warn("第 {} 页 {}DPI 渲染失败，尝试默认渲染: {}", page, dpi, e1.getMessage());
                try {
                    image = renderer.renderImage(page);
                    log.debug("第 {} 页使用默认渲染成功", page);
                } catch (Exception e2) {
                    log.error("第 {} 页渲染失败，跳过此页: {}", page, e2.getMessage());
                    return null;
                }
            }

            if (image == null) {
                log.warn("第 {} 页渲染结果为null，跳过", page);
                return null;
            }

            log.debug("第 {} 页图像尺寸: {}x{}", page, image.getWidth(), image.getHeight());

            // 启用图像预处理：灰度转换 + 对比度增强 + Otsu二值化 + 中值滤波降噪
            BufferedImage processed = preprocessImage(image);
            long preprocessTime = System.currentTimeMillis() - startTime;
            log.debug("第 {} 页图像预处理完成，耗时: {} ms", page, preprocessTime);

            // 保存预处理后的图像
            File imageFile = new File(tempDir, "page_" + page + ".png");
            try {
                ImageIO.write(processed, "png", imageFile);
                log.debug("第 {} 页预处理图像已保存: {} ({} bytes)", page, imageFile.getAbsolutePath(), imageFile.length());
            } catch (IOException e) {
                log.warn("第 {} 页PNG保存失败，尝试JPEG: {}", page, e.getMessage());
                try {
                    ImageIO.write(processed, "jpg", imageFile);
                } catch (IOException e2) {
                    log.error("第 {} 页图像保存失败，跳过: {}", page, e2.getMessage());
                    return null;
                }
            }

            String outputBase = new File(tempDir, "page_" + page).getAbsolutePath();

            // 调用 tesseract OCR
            ProcessBuilder pb = new ProcessBuilder(
                    "cmd", "/c",
                    TESSERACT_PATH,
                    imageFile.getAbsolutePath(),
                    outputBase,
                    "-l", "chi_sim+eng",
                    "--psm", "3",  // PSM 3: 自动页面分割
                    "-c", "tessedit_do_invert=0"  // 禁用自动反色
            );
            pb.redirectErrorStream(true);
            log.debug("执行 OCR 命令：第 {} 页", page);

            long ocrStartTime = System.currentTimeMillis();
            Process process = pb.start();
            int exitCode = process.waitFor();
            long ocrDuration = System.currentTimeMillis() - ocrStartTime;
            
            log.debug("第 {} 页 Tesseract 退出码: {}，OCR耗时: {} ms", page, exitCode, ocrDuration);

            if (exitCode == 0) {
                File txtFile = new File(outputBase + ".txt");
                if (txtFile.exists() && txtFile.length() > 0) {
                    String pageText = new String(Files.readAllBytes(txtFile.toPath()), StandardCharsets.UTF_8);
                    String trimmed = pageText.trim();
                    if (!trimmed.isEmpty()) {
                        long totalTime = System.currentTimeMillis() - startTime;
                        log.info("第 {} 页 OCR 成功，提取 {} 字符，总耗时: {} ms（预处理{}ms + OCR{}ms）", 
                                page, trimmed.length(), totalTime, preprocessTime, ocrDuration);
                        return trimmed;
                    } else {
                        log.warn("第 {} 页 OCR 生成的文件为空", page);
                        return null;
                    }
                } else {
                    log.warn("第 {} 页 OCR 未生成输出文件", page);
                    return null;
                }
            } else {
                log.warn("第 {} 页 OCR 失败（退出码: {}）", page, exitCode);
                return null;
            }
        } catch (Exception e) {
            log.error("第 {} 页处理异常", page, e);
            return null;
        }
    }

    /**
     * OCR 扫描版 PDF（使用绝对路径 + 图像预处理 + 多种渲染方案）
     */
    private String ocrPdfWithTesseract(File pdfFile) {
        return ocrPdfWithTesseract(pdfFile, null);
    }

    /**
     * 图像预处理（优化版）：灰度转换 + 对比度增强 + Otsu二值化 + 中值滤波降噪
     * 采用 ColorConvertOp 硬件加速灰度、Otsu 自适应阈值、中值滤波替代逐像素遍历
     */
    private BufferedImage preprocessImage(BufferedImage original) {
        try {
            int width = original.getWidth();
            int height = original.getHeight();

            // 步骤1：灰度转换 —— 使用 ColorConvertOp 利用 JVM 底层硬件加速，比逐像素快10倍以上
            BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            java.awt.image.ColorConvertOp cco = new java.awt.image.ColorConvertOp(
                    original.getColorModel().getColorSpace(),
                    gray.getColorModel().getColorSpace(), null);
            cco.filter(original, gray);

            // 步骤2：对比度增强（直方图拉伸），改善扫描件灰暗问题
            int[] histogram = new int[256];
            int totalPixels = width * height;
            byte[] pixels = ((java.awt.image.DataBufferByte) gray.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < pixels.length; i++) {
                histogram[pixels[i] & 0xFF]++;
            }
            // 找到有效灰度范围 [pLow, pHigh]，忽略两端各0.5%的极端像素
            int pLow = 0, pHigh = 255;
            int cumLow = (int) (totalPixels * 0.005);
            int cumHigh = (int) (totalPixels * 0.995);
            int cumSum = 0;
            for (int i = 0; i < 256; i++) {
                cumSum += histogram[i];
                if (cumSum < cumLow) pLow = i;
                if (cumSum < cumHigh) pHigh = i;
            }
            // 线性拉伸到 [0, 255]
            int range = pHigh - pLow;
            if (range > 10) { // 仅在对比度确实不足时拉伸
                for (int i = 0; i < pixels.length; i++) {
                    int v = pixels[i] & 0xFF;
                    int nv = ((v - pLow) * 255) / range;
                    pixels[i] = (byte) Math.max(0, Math.min(255, nv));
                }
            }

            // 步骤3：Otsu 大津法自动阈值二值化 —— 基于类间方差最大化，自适应确定最佳阈值
            int[] hist = new int[256];
            for (int i = 0; i < pixels.length; i++) {
                hist[pixels[i] & 0xFF]++;
            }
            double sum = 0;
            for (int i = 0; i < 256; i++) sum += i * hist[i];
            double sumB = 0;
            int wB = 0, wF = 0;
            double maxVariance = 0;
            int otsuThreshold = 0;
            for (int t = 0; t < 256; t++) {
                wB += hist[t];
                if (wB == 0) continue;
                wF = totalPixels - wB;
                if (wF == 0) break;
                sumB += (double) t * hist[t];
                double mB = sumB / wB;
                double mF = (sum - sumB) / wF;
                double variance = (double) wB * wF * (mB - mF) * (mB - mF);
                if (variance > maxVariance) {
                    maxVariance = variance;
                    otsuThreshold = t;
                }
            }
            log.debug("Otsu 自动阈值: {}", otsuThreshold);

            // 应用二值化
            BufferedImage binary = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
            byte[] outPixels = ((java.awt.image.DataBufferByte) binary.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < pixels.length; i++) {
                outPixels[i] = ((pixels[i] & 0xFF) < otsuThreshold) ? (byte) 0 : (byte) 255;
            }

            // 步骤4：中值滤波降噪 —— 3×3中值滤波，有效去除椒盐噪声，同时保留文字边缘
            byte[] denoised = new byte[outPixels.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (y == 0 || y == height - 1 || x == 0 || x == width - 1) {
                        denoised[y * width + x] = outPixels[y * width + x];
                        continue;
                    }
                    // 收集3×3邻域像素值
                    int[] window = new int[9];
                    int idx = 0;
                    for (int j = -1; j <= 1; j++) {
                        for (int i = -1; i <= 1; i++) {
                            window[idx++] = outPixels[(y + j) * width + (x + i)] & 0xFF;
                        }
                    }
                    // 排序取中值
                    Arrays.sort(window);
                    denoised[y * width + x] = (byte) window[4];
                }
            }

            // 将去噪结果写回二值图像
            byte[] finalPixels = ((java.awt.image.DataBufferByte) binary.getRaster().getDataBuffer()).getData();
            System.arraycopy(denoised, 0, finalPixels, 0, denoised.length);

            log.debug("图像预处理完成：{}x{}，Otsu阈值={}", width, height, otsuThreshold);
            return binary;

        } catch (Exception e) {
            log.error("图像预处理异常，返回原图", e);
            return original;
        }
    }

    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        String[] sentences = text.split("(?<=[.!?。！？\n])");
        StringBuilder cur = new StringBuilder();
        for (String s : sentences) {
            if (cur.length() + s.length() > chunkSize && cur.length() > 0) {
                chunks.add(cur.toString());
                int keep = Math.min(overlap, cur.length());
                cur = new StringBuilder(cur.substring(cur.length() - keep));
            }
            cur.append(s);
        }
        if (cur.length() > 0) chunks.add(cur.toString());
        return chunks;
    }

    // ========================== 检索（TF-IDF） ==========================
    @Override
    public Result<List<String>> searchKnowledge(Integer userId, String question, int topK) {
        List<RagKnowledge> docs = baseMapper.selectByUserId(userId);
        if (docs.isEmpty()) return Result.success("知识库为空", new ArrayList<>());

        List<List<String>> docTokens = new ArrayList<>();
        Map<String, Integer> df = new HashMap<>();
        for (RagKnowledge doc : docs) {
            List<String> tokens = tokenize(doc.getChunkContent());
            docTokens.add(tokens);
            Set<String> unique = new HashSet<>(tokens);
            for (String t : unique) df.merge(t, 1, Integer::sum);
        }

        List<String> queryTokens = tokenize(question);
        Map<String, Double> queryTf = new HashMap<>();
        for (String t : queryTokens) queryTf.merge(t, 1.0, Double::sum);
        double queryNorm = 0.0;
        for (double v : queryTf.values()) queryNorm += v * v;
        queryNorm = Math.sqrt(queryNorm);

        int N = docs.size();
        PriorityQueue<Scored> heap = new PriorityQueue<>((a, b) -> Double.compare(b.score, a.score));
        for (int i = 0; i < N; i++) {
            Map<String, Double> docTf = new HashMap<>();
            for (String t : docTokens.get(i)) docTf.merge(t, 1.0, Double::sum);
            double dot = 0.0, docNorm = 0.0;
            for (Map.Entry<String, Double> e : docTf.entrySet()) {
                String term = e.getKey();
                double tf = e.getValue();
                double idf = Math.log((N + 1.0) / (df.getOrDefault(term, 0) + 1)) + 1.0;
                double weight = tf * idf;
                docNorm += weight * weight;
                if (queryTf.containsKey(term)) {
                    double qWeight = queryTf.get(term) * idf;
                    dot += qWeight * weight;
                }
            }
            docNorm = Math.sqrt(docNorm);
            if (docNorm > 0 && queryNorm > 0) {
                double sim = dot / (docNorm * queryNorm);
                heap.add(new Scored(docs.get(i).getChunkContent(), sim));
            }
        }

        List<String> result = new ArrayList<>();
        int count = 0;
        while (!heap.isEmpty() && count < topK) {
            result.add(heap.poll().content);
            count++;
        }
        return Result.success("检索完成", result);
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("[a-zA-Z]+|[\\u4e00-\\u9fa5]");
        java.util.regex.Matcher m = p.matcher(text);
        while (m.find()) {
            String seg = m.group();
            if (seg.matches("[a-zA-Z]+")) {
                tokens.add(seg.toLowerCase());
            } else {
                for (char c : seg.toCharArray()) tokens.add(String.valueOf(c));
                for (int i = 0; i < seg.length() - 1; i++) tokens.add(seg.substring(i, i + 2));
                for (int i = 0; i < seg.length() - 2; i++) tokens.add(seg.substring(i, i + 3));
            }
        }
        return tokens;
    }

    private static class Scored {
        String content;
        double score;
        Scored(String c, double s) { content = c; score = s; }
    }

    // ========================== 其他 CRUD ==========================
    @Override
    @Transactional
    public Result<String> deleteUserKnowledge(Integer userId) {
        int count = baseMapper.deleteByUserId(userId);
        return Result.success("删除完成，共" + count + "条");
    }

    @Override
    @Transactional
    public Result<String> deleteMaterialKnowledge(Integer studyMaterialId) {
        int count = baseMapper.deleteByStudyMaterialId(studyMaterialId);
        return Result.success("删除完成，共" + count + "条");
    }

    @Override
    public Result<Object> getKnowledgeStats(Integer userId) {
        int chunks = baseMapper.countByUserId(userId);
        int materials = baseMapper.countMaterialsByUserId(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChunks", chunks);
        stats.put("totalMaterials", materials);
        stats.put("status", chunks > 0 ? "已启用" : "未启用");
        return Result.success("获取统计信息成功", stats);
    }

    @Override
    @Transactional
    public Result<String> parseAndStoreGradingMaterial(StudyMaterial studyMaterial, Integer taskId) {
        log.info("开始解析阅卷参考材料 - ID: {}, 文件名: {}", studyMaterial.getId(), studyMaterial.getOriginalName());
        try {
            File file = new File(studyMaterial.getFilePath());
            if (!file.exists()) {
                if (taskId != null) ragParseTaskService.markFailed(taskId, "文件不存在");
                return Result.failed("文件不存在");
            }

            String content = extractTextByFileType(file, studyMaterial.getFileType(), taskId);
            if (content == null || content.trim().isEmpty()) {
                if (taskId != null) ragParseTaskService.markFailed(taskId, "文本提取失败");
                return Result.failed("文件无法识别文本内容");
            }

            List<String> chunks = splitText(content, 800, 200);
            if (chunks.isEmpty()) {
                if (taskId != null) ragParseTaskService.markFailed(taskId, "文本分块失败");
                return Result.failed("文本处理失败");
            }

            baseMapper.deleteByStudyMaterialId(studyMaterial.getId());

            for (int i = 0; i < chunks.size(); i++) {
                RagKnowledge knowledge = new RagKnowledge();
                knowledge.setUserId(studyMaterial.getUserId());
                knowledge.setStudyMaterialId(studyMaterial.getId());
                knowledge.setChunkContent(chunks.get(i));
                knowledge.setChunkIndex(i);
                knowledge.setKnowledgeType("grading");
                save(knowledge);

                if (taskId != null && (i + 1) % 10 == 0) {
                    int progress = 50 + (int)((i + 1) * 50.0 / chunks.size());
                    ragParseTaskService.updateProgress(taskId, progress, 0);
                }
            }

            if (taskId != null) ragParseTaskService.markCompleted(taskId, chunks.size());
            log.info("阅卷材料解析完成 - 总块数: {}", chunks.size());
            return Result.success("解析成功，共" + chunks.size() + "块阅卷参考知识");

        } catch (Exception e) {
            log.error("解析阅卷参考材料异常", e);
            if (taskId != null) ragParseTaskService.markFailed(taskId, e.getMessage());
            return Result.failed("解析失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<String>> searchGradingKnowledge(Integer userId, String question, int topK) {
        List<RagKnowledge> docs = baseMapper.selectByUserIdAndType(userId, "grading");
        if (docs.isEmpty()) return Result.success("阅卷知识库为空", new ArrayList<>());

        List<List<String>> docTokens = new ArrayList<>();
        Map<String, Integer> df = new HashMap<>();
        for (RagKnowledge doc : docs) {
            List<String> tokens = tokenize(doc.getChunkContent());
            docTokens.add(tokens);
            Set<String> unique = new HashSet<>(tokens);
            for (String t : unique) df.merge(t, 1, Integer::sum);
        }

        List<String> queryTokens = tokenize(question);
        Map<String, Double> queryTf = new HashMap<>();
        for (String t : queryTokens) queryTf.merge(t, 1.0, Double::sum);
        double queryNorm = 0.0;
        for (double v : queryTf.values()) queryNorm += v * v;
        queryNorm = Math.sqrt(queryNorm);

        int N = docs.size();
        PriorityQueue<Scored> heap = new PriorityQueue<>((a, b) -> Double.compare(b.score, a.score));
        for (int i = 0; i < N; i++) {
            Map<String, Double> docTf = new HashMap<>();
            for (String t : docTokens.get(i)) docTf.merge(t, 1.0, Double::sum);
            double dot = 0.0, docNorm = 0.0;
            for (Map.Entry<String, Double> e : docTf.entrySet()) {
                String term = e.getKey();
                double tf = e.getValue();
                double idf = Math.log((N + 1.0) / (df.getOrDefault(term, 0) + 1)) + 1.0;
                double weight = tf * idf;
                docNorm += weight * weight;
                if (queryTf.containsKey(term)) {
                    double qWeight = queryTf.get(term) * idf;
                    dot += qWeight * weight;
                }
            }
            docNorm = Math.sqrt(docNorm);
            if (docNorm > 0 && queryNorm > 0) {
                double sim = dot / (docNorm * queryNorm);
                heap.add(new Scored(docs.get(i).getChunkContent(), sim));
            }
        }

        List<String> result = new ArrayList<>();
        int count = 0;
        while (!heap.isEmpty() && count < topK) {
            result.add(heap.poll().content);
            count++;
        }
        return Result.success("检索完成", result);
    }

    @Override
    public Result<Object> getGradingKnowledgeStats(Integer userId) {
        int chunks = baseMapper.countByUserIdAndType(userId, "grading");
        int materials = baseMapper.countMaterialsByUserIdAndType(userId, "grading");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChunks", chunks);
        stats.put("totalMaterials", materials);
        stats.put("status", chunks > 0 ? "已启用" : "未启用");
        return Result.success("获取阅卷知识库统计成功", stats);
    }

    @Override
    @Transactional
    public Result<String> deleteGradingKnowledge(Integer userId) {
        int count = baseMapper.deleteByUserIdAndType(userId, "grading");
        return Result.success("删除完成，共" + count + "条阅卷知识库数据");
    }

    @Override
    public Result<List<String>> searchAdminGradingKnowledge(String question, int topK) {
        List<RagKnowledge> docs = baseMapper.selectByType("grading");
        if (docs.isEmpty()) return Result.success("管理员阅卷知识库为空", new ArrayList<>());

        List<List<String>> docTokens = new ArrayList<>();
        Map<String, Integer> df = new HashMap<>();
        for (RagKnowledge doc : docs) {
            List<String> tokens = tokenize(doc.getChunkContent());
            docTokens.add(tokens);
            Set<String> unique = new HashSet<>(tokens);
            for (String t : unique) df.merge(t, 1, Integer::sum);
        }

        List<String> queryTokens = tokenize(question);
        Map<String, Double> queryTf = new HashMap<>();
        for (String t : queryTokens) queryTf.merge(t, 1.0, Double::sum);
        double queryNorm = 0.0;
        for (double v : queryTf.values()) queryNorm += v * v;
        queryNorm = Math.sqrt(queryNorm);

        int N = docs.size();
        PriorityQueue<Scored> heap = new PriorityQueue<>((a, b) -> Double.compare(b.score, a.score));
        for (int i = 0; i < N; i++) {
            Map<String, Double> docTf = new HashMap<>();
            for (String t : docTokens.get(i)) docTf.merge(t, 1.0, Double::sum);
            double dot = 0.0, docNorm = 0.0;
            for (Map.Entry<String, Double> e : docTf.entrySet()) {
                String term = e.getKey();
                double tf = e.getValue();
                double idf = Math.log((N + 1.0) / (df.getOrDefault(term, 0) + 1)) + 1.0;
                double weight = tf * idf;
                docNorm += weight * weight;
                if (queryTf.containsKey(term)) {
                    double qWeight = queryTf.get(term) * idf;
                    dot += qWeight * weight;
                }
            }
            docNorm = Math.sqrt(docNorm);
            if (docNorm > 0 && queryNorm > 0) {
                double sim = dot / (docNorm * queryNorm);
                heap.add(new Scored(docs.get(i).getChunkContent(), sim));
            }
        }

        List<String> result = new ArrayList<>();
        int count = 0;
        while (!heap.isEmpty() && count < topK) {
            result.add(heap.poll().content);
            count++;
        }
        return Result.success("检索完成", result);
    }

    @Override
    public Result<Object> getAdminGradingKnowledgeStats() {
        int chunks = baseMapper.countByType("grading");
        int materials = baseMapper.countMaterialsByType("grading");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChunks", chunks);
        stats.put("totalMaterials", materials);
        stats.put("status", chunks > 0 ? "已启用" : "未启用");
        return Result.success("获取管理员阅卷知识库统计成功", stats);
    }

    @Override
    @Transactional
    public Result<String> deleteAdminGradingKnowledge() {
        int count = baseMapper.deleteByType("grading");
        return Result.success("删除完成，共" + count + "条管理员阅卷知识库数据");
    }
}