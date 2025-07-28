package org.sopt.confeti;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlaywrightWarmup {

    @PostConstruct
    public void warmUp() {
        log.info("🔄 Playwright Warm-up 시작...");

        long startTime = System.currentTimeMillis();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setChannel("chromium"));
             BrowserContext context = browser.newContext();
             Page page = context.newPage()) {

            String libVersion = Playwright.class.getPackage().getImplementationVersion();
            log.info("📦 [Playwright] 라이브러리 버전: {}", libVersion != null ? libVersion : "알 수 없음");
            log.info("✅ [Playwright] 브라우저 실행 완료: version={}", browser.version());

            page.navigate("about:blank");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.pdf(new Page.PdfOptions().setPrintBackground(true).setWidth("800px").setHeight("1000px"));

            log.info("✅ Playwright Warm-up 완료! ({}ms)", (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            log.error("❌ Playwright Warm-up 실패", e);
        }
    }

}