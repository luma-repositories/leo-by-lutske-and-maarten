package com.luma.ocr;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.nio.file.*;

public class ItalianOcr {

    private final Tesseract tesseract;

    public ItalianOcr(Path tessDataPath) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessDataPath.toString());
        this.tesseract.setLanguage("ita");
        this.tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO);
    }

    public String process(Path image) throws TesseractException {
        return tesseract.doOCR(image.toFile());
    }

    static void main(String[] args) {
        Path tessDataPath = args.length >= 1
                ? Path.of(args[0])
                : Path.of(System.getenv().getOrDefault("TESSDATA_PREFIX", "tessdata"));
        ItalianOcr ocr = new ItalianOcr(tessDataPath);
        new ItalianOcrCli(ocr).run();
    }

}

