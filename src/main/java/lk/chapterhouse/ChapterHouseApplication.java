package lk.chapterhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class ChapterHouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChapterHouseApplication.class, args);
    }
}
