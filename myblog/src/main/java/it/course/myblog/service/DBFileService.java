package it.course.myblog.service;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import it.course.myblog.entity.DBFile;
import it.course.myblog.repository.DBFileRepository;

@Service
public class DBFileService {

    @Autowired
    DBFileRepository dbFileRepository;
    
    @Value("${post.image.width}")
    int width;
    
    @Value("${post.image.heigth}")
    int heigth;
    

    public DBFile fromMultiToDBFile(MultipartFile file) {

    	String fileName = StringUtils.cleanPath(file.getOriginalFilename());

    	DBFile dbFile = null;
		try {
			dbFile = new DBFile(fileName, file.getContentType(), file.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

    	return dbFile;
    }
    
    public BufferedImage getBufferedImage(MultipartFile dbFile) {
    	
    	BufferedImage image = null;
		try {
			image = ImageIO.read(dbFile.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
		
    }
    
    public String ctrlImageSize(BufferedImage bf) { 
		
    	String s = null; 
		if(bf != null) {
			if(bf.getWidth() != width || bf.getHeight() != heigth ) {
				return "The image size must be "+width+"x"+heigth;
			}
		}
		return null;
    }
    
}