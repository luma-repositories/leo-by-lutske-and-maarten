package be.lutske.leolegacy.interfaceadapter.rest;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class RecipeImportForm {

    @RestForm("file")
    public FileUpload file;
}
