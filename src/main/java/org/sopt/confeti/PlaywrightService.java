package org.sopt.confeti;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.ScreenshotOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Clip;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SameSiteAttribute;
import com.microsoft.playwright.options.ScreenshotType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaywrightService implements PlaywrightUseCase{


    @Override
    public byte[] generatePdf(PlaywrightCommand command) {
        long startTime = System.nanoTime();
        try (
                Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chromium"));
                BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                        .setViewportSize(command.getWidth(), 1080)
                        .setJavaScriptEnabled(true));
                Page page = context.newPage();
        ) {
            Cookie cookie = new Cookie("accessToken", command.getAccessToken())
                    .setDomain("www.confeti.co.kr")
                    .setPath("/")
                    .setHttpOnly(false)
                    .setSecure(true)
                    .setSameSite(SameSiteAttribute.NONE);
            context.addCookies(List.of(cookie));

            log.info("[PDF] Playwright 초기화 완료. ({} ms)", elapsed(startTime));

            long navStart = System.nanoTime();
            page.navigate(command.getUrl());
            log.info("[PDF] 페이지 네비게이션 완료. ({} ms)", elapsed(navStart));

            long loadStart = System.nanoTime();
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            log.info("[PDF] 페이지 로드 완료. ({} ms)", elapsed(loadStart));

            page.waitForTimeout(5000);
            log.info("[PDF] waitForTimeout(2000) 완료");

            long pdfGenStart = System.nanoTime();
            byte[] pngBytes = page.screenshot(
                    new ScreenshotOptions()
                            .setType(ScreenshotType.PNG)
                            .setFullPage(false)
                            .setOmitBackground(false)
                            .setClip(0, 0, command.getWidth(), command.getHeight())
            );
//            byte[] pdfBytes = page.pdf(new Page.PdfOptions()
//                    .setPrintBackground(true)
//                    .setWidth(command.getWidth() + "px")
//                    .setHeight(command.getHeight() + "px"));
            log.info("[PDF] PDF 생성 완료. ({} ms)", elapsed(pdfGenStart));

            return pngBytes;
        }
    }

    private long elapsed(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
