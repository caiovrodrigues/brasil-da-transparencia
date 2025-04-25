package com.br.transparencia.etl.load;

import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class Tjdft {

    private static final String TJDFT_URL = "https://rh.tjdft.jus.br/Transparencia/folhadepagamento/detalhamentofolha.asp";
    private static final Logger log = LoggerFactory.getLogger(Tjdft.class);

    @EventListener(ApplicationReadyEvent.class)
    void startup(){
        try(
                Playwright playWright = Playwright.create();
                Browser browser = playWright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true).setSlowMo(5_000))
        ){
            Page page = browser.newPage();
            page.navigate(TJDFT_URL);
            page.onDownload(handler -> {
                try(var inputStream = handler.createReadStream()){
                    byte[] bytes = inputStream.readAllBytes();
                    Files.write(Paths.get("tjdft.csv"), bytes);
                }catch (Exception e){
                    log.error("Não foi possível baixar o arquivo", e);
                }
            });
            page.click("button.g-recaptcha");
            page.click("#spanExportacoes > a:nth-child(3)");
        }
    }
}

