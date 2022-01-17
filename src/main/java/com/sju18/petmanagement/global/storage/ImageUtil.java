package com.sju18.petmanagement.global.storage;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class ImageUtil {
    public final static int ORIGINAL_IMAGE = 1;
    public final static int GENERAL_IMAGE = 2;
    public final static int THUMBNAIL_IMAGE = 3;

    public final static int NOT_IMAGE = 4;

    private final static int THUMBNAIL_SIZE = 500;
    private final static int GENERAL_SIZE = 1000;

    // 이미지 경로 생성
    public static String createImageUrl(String filePath, int imageType) {
        String defaultFilePath = filePath.split("\\.")[0];
        String fileFormat = filePath.split("\\.")[1];

        // 손실 압축 이미지 포맷인 JPEG, JPG, WEBP 은 imageType에 따라 파일 URL 생성
        if(fileFormat.equals("jpeg") || fileFormat.equals("jpg") || fileFormat.equals("webp")) {
            if(imageType == ImageUtil.ORIGINAL_IMAGE) {
                return defaultFilePath + "original." + fileFormat;
            }
            else if(imageType == ImageUtil.GENERAL_IMAGE) {
                return defaultFilePath + "general." + fileFormat;
            }
            else if(imageType == ImageUtil.THUMBNAIL_IMAGE) {
                return defaultFilePath + "thumbnail." + fileFormat;
            }
            else {
                return defaultFilePath;
            }
        }
        // 무손실 압축 이미지 포맷인 PNG, GIF 은 ORIGINAL_IMAGE 파일 URL 생성
        else {
            return defaultFilePath + "original." + fileFormat;
        }
    }

    // 이미지 파일 최적화 및 여러 버전으로 저장
    public static void optimizeAndSaveImage(String fileName, MultipartFile uploadedFile, Path savePath) throws Exception {
        // 업로드 된 파일 확장자
        String fileFormat = FileUtils.getExtension(Objects.requireNonNull(uploadedFile.getOriginalFilename()));

        // 손실 압축 이미지 포맷인 JPEG, JPG, WEBP 은 Resize/Crop 후 3개의 파일로 저장
        if(fileFormat.equals("jpeg") || fileFormat.equals("jpg") || fileFormat.equals("webp")) {
            String originalFileName = fileName + "original." + fileFormat;
            String generalFileName = fileName + "general." + fileFormat;
            String thumbnailFileName = fileName + "thumbnail." + fileFormat;

            // 원본 이미지 저장 후 이미지 파일 불러오기
            uploadedFile.transferTo(savePath.resolve(originalFileName));
            File originalFile = new File(savePath.resolve(originalFileName).toString());

            // 원본 파일을 미리보기, 일반 버전 파일로 후처리 후 저장
            BufferedImage generalImage = resizeToGeneralImage(originalFile);
            BufferedImage thumbnailImage = resizeToThumbnailImage(originalFile);

            ImageIO.write(generalImage, fileFormat, savePath.resolve(generalFileName).toFile());
            ImageIO.write(thumbnailImage, fileFormat, savePath.resolve(thumbnailFileName).toFile());
        }
        // 무손실 압축 이미지 포맷인 PNG, GIF은 Resize 없이 원본 파일만 저장
        else {
            String originalFileName = fileName + "original." + fileFormat;
            uploadedFile.transferTo(savePath.resolve(originalFileName));
        }
    }

    public static BufferedImage resizeToThumbnailImage(File originalFile) throws Exception {
        BufferedImage adjustedImage = adjustRotatedImage(originalFile);
        BufferedImage resizedImage = Scalr.resize(adjustedImage,Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH, THUMBNAIL_SIZE, Scalr.OP_ANTIALIAS);

        int imageHeight = resizedImage.getHeight();
        if (imageHeight >= THUMBNAIL_SIZE) {
            return Scalr.crop(resizedImage, 0, (imageHeight - THUMBNAIL_SIZE ) /2, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        } else {
            return resizedImage;
        }
    }

    public static BufferedImage resizeToGeneralImage(File originalFile) throws Exception {
        BufferedImage adjustedImage = adjustRotatedImage(originalFile);
        return Scalr.resize(adjustedImage, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH, GENERAL_SIZE, Scalr.OP_ANTIALIAS);
    }

    public static BufferedImage adjustRotatedImage(File originalFile) throws Exception {
        final int DEG_0 = 1;
        final int DEG_180 = 3;
        final int DEG_270 = 6;
        final int DEG_90 = 8;

        int orientation = DEG_0;

        Metadata metadata;
        Directory directory;

        try {
            metadata = ImageMetadataReader.readMetadata(originalFile);
            directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if(directory != null) {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch(Exception ignored) {
            // 회전값 읽기 오류시 자동회전 전처리 진행하지 않고 통과
        }

        BufferedImage image = ImageIO.read(originalFile);

        switch(orientation) {
            case DEG_180:
                image = Scalr.rotate(image, Scalr.Rotation.CW_180);
                break;
            case DEG_270:
                image = Scalr.rotate(image, Scalr.Rotation.CW_90);
                break;
            case DEG_90:
                image = Scalr.rotate(image, Scalr.Rotation.CW_270);
                break;
            case DEG_0:
            default:
                break;
        }

        return image;
    }
}
