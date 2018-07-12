package com.renansouza.processor;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Constants {

    @Getter
    private static final String[] allExtensions = {"xml", "zip", "7z"};
    @Getter
    private static final String[] compressedExtensions = {"zip", "7z"};
    @Getter
    private static final String[] environment = {"Nfe_Teste", "Nfe_Producao"};
    @Getter
    private static final String[] flow = {"Emissao", "Recebimento"};

}