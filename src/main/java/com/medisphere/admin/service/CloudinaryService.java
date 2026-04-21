package com.medisphere.admin.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Uploads a doctor licence image to Cloudinary under the "medisphere/licences" folder.
     * @param file the image file to upload
     * @return the secure URL of the uploaded image
     */
    public String uploadLicenceImage(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "medisphere/licences",
                        "resource_type", "image"
                )
        );
        return (String) result.get("secure_url");
    }
}
