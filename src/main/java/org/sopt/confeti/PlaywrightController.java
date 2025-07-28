package org.sopt.confeti;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PlaywrightController {

    private final PlaywrightUseCase playwrightUseCase;

    @PostMapping("/generate")
    public ResponseEntity<Resource> generate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken,
            @RequestBody GeneratePdfInfo generatePdfInfo
    ) {
        if (!accessToken.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                    .body(null);
        }
        accessToken = accessToken.substring("Bearer ".length());
        log.info("Access Token : {}", accessToken);

        PlaywrightCommand command = PlaywrightCommand.builder()
                .url(generatePdfInfo.getUrl())
                .accessToken(accessToken)
                .width(generatePdfInfo.getWidth())
                .height(generatePdfInfo.getHeight())
                .build();

        String filename = "output.pdf";
        byte[] pdfBytes = playwrightUseCase.generatePdf(command);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pdfBytes);
        InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(pdfBytes.length);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
