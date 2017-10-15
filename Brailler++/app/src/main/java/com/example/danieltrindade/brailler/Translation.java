package com.example.danieltrindade.brailler;

import android.util.Log;

/**
 * Created by Daniel Trindade on 12/04/2017.
 */

public class Translation {

    private static final String TAG = "Brailler++/Translation";

    //Context _context;
    MainActivity _main;
    AlfabetoBraille alfabetoBraille = new AlfabetoBraille();

    public Translation(MainActivity main){
        _main=main;
    }

    public String translateData(String data){
        //long sequence = Long.valueOf(data);

        Log.v(TAG, "orientation " + _main.posicaoPredefinidaDoTelemovel);

        // posicao vertical
        //_main.mobilePosition = _main.getRotation(_main,_main.orientMe);

        //espaco
        if (data.equals(_main.posicaoEspaco)){
            return "Espaço";
        }

        //backspace
        if (data.equals(_main.posicaoApagar)){
            return "Apagar";
        }

        //outra opcao
        if (data.equals(_main.posicaoEnter)){
            return "Enter";
        }

        //carater
        else{
            if (_main.numberFlag){
                return translateToNumber(data,_main.posicaoPredefinidaDoTelemovel);
            }
            else if (_main.lettersLowerCase){
                String caraterProduzido = translateToChar(data,_main.posicaoPredefinidaDoTelemovel);

                //para o logging
                if (caraterProduzido.equals("fim da frase"))return "fim da frase";

                if (caraterProduzido.matches("[a-zA-Z]+")){
                    caraterProduzido = caraterProduzido.toLowerCase();
                }
                return caraterProduzido;
            }
            else if (_main.lettersUpperCase){
                String caraterProduzido = translateToChar(data,_main.posicaoPredefinidaDoTelemovel);
                if (caraterProduzido.matches("[a-zA-Z]+") || caraterProduzido.matches(".*[çáéíóúàèìùâêôãõïü].*")){
                    caraterProduzido = caraterProduzido.toUpperCase();
                }
                return caraterProduzido;
            }
            //modo de edição
            else{
                String edicaoProduzida = translateToEditTask(data,_main.posicaoPredefinidaDoTelemovel);
                return edicaoProduzida;
            }
        }
    }

    private String translateToNumber(String data, String posicao) {

        if (posicao.equals("portrait esquerda")) {
            for (int i = 1; i < alfabetoBraille.NUMBER_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.numbers_Portrait_esquerda[i])) {
                    return alfabetoBraille.numbers_Portrait_esquerda[i - 1];
                }
            }
            return "";
        }
        if (posicao.equals("portrait direita")) {
            for (int i = 1; i < alfabetoBraille.NUMBER_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.numbers_Portrait_direita[i])) {
                    return alfabetoBraille.numbers_Portrait_direita[i - 1];
                }
            }
            return "";
        }
        if(posicao.equals("landscape esquerda")){
            for(int i = 1; i<alfabetoBraille.NUMBER_ARRAY_SIZE; i++){
                if(data.equals(alfabetoBraille.numbers_Landscape_To_The_Right_esquerda[i])){
                    return alfabetoBraille.numbers_Landscape_To_The_Right_esquerda[i - 1];
                }
            }
            return "";
        }
        if(posicao.equals("landscape direita")){
            for(int i = 1; i<alfabetoBraille.NUMBER_ARRAY_SIZE; i++){
                if(data.equals(alfabetoBraille.numbers_Landscape_To_The_Right_direita[i])){
                    return alfabetoBraille.numbers_Landscape_To_The_Right_direita[i - 1];
                }
            }
            return "";
        }
        if(posicao.equals("reverse landscape esquerda")){
            for(int i = 1; i<alfabetoBraille.NUMBER_ARRAY_SIZE; i++){
                if(data.equals(alfabetoBraille.numbers_Landscape_To_The_Left_esquerda[i])){
                    return alfabetoBraille.numbers_Landscape_To_The_Left_esquerda[i - 1];
                }
            }
            return "";
        }
        if(posicao.equals("reverse landscape direita")){
            for(int i = 1; i<alfabetoBraille.NUMBER_ARRAY_SIZE; i++){
                if(data.equals(alfabetoBraille.numbers_Landscape_To_The_Left_direita[i])){
                    return alfabetoBraille.numbers_Landscape_To_The_Left_direita[i - 1];
                }
            }
            return "";
        }

        return "";
    }

    //falta para as outras posicoes
    public  String translateToEditTask(String data, String posicao){

        if (posicao.equals("reverse landscape esquerda")) {
            for (int i=0; i < alfabetoBraille.EDIT_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.edit_Landscape_To_The_Left_esquerda[i])){
                    return alfabetoBraille.edit_Landscape_To_The_Left_esquerda[i-1];
                }
            }
            return "";
        }

        return "";
    }

    public String translateToChar(String data, String posicao){

        if (posicao.equals("portrait esquerda")) {
            for (int i = 1; i < alfabetoBraille.CHAR_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.chars_Portrait_esquerda[i])) {
                    return alfabetoBraille.chars_Portrait_esquerda[i-1];
                }
            }
            return "";
        }
        if (posicao.equals("portrait direita")) {
            for (int i = 1; i < alfabetoBraille.CHAR_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.chars_Portrait_direita[i])) {
                    return alfabetoBraille.chars_Portrait_direita[i-1];
                }
            }
            return "";
        }
        if (posicao.equals("landscape esquerda")) {
            for (int i = 1; i < alfabetoBraille.CHAR_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.chars_Landscape_To_The_Right_esquerda[i])) {
                    return alfabetoBraille.chars_Landscape_To_The_Right_esquerda[i-1];
                }
            }
            return "";
        }
        if (posicao.equals("landscape direita")) {
            for (int i = 1; i < alfabetoBraille.CHAR_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.chars_Landscape_To_The_Right_direita[i])) {
                    return alfabetoBraille.chars_Landscape_To_The_Right_direita[i-1];
                }
            }
            return "";
        }
        if (posicao.equals("landscape esquerda")) {
            for (int i = 1; i < alfabetoBraille.CHAR_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.chars_Landscape_To_The_Right_esquerda[i])) {
                    return alfabetoBraille.chars_Landscape_To_The_Right_esquerda[i-1];
                }
            }
            return "";
        }
        if (posicao.equals("landscape direita")) {
            for (int i = 1; i < alfabetoBraille.CHAR_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.chars_Landscape_To_The_Right_direita[i])) {
                    return alfabetoBraille.chars_Landscape_To_The_Right_direita[i-1];
                }
            }
            return "";
        }
        if (posicao.equals("reverse landscape esquerda")) {
            for (int i=0; i < alfabetoBraille.CHAR_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.chars_Landscape_To_The_Left_esquerda[i])){
                    return alfabetoBraille.chars_Landscape_To_The_Left_esquerda[i-1];
                }
            }
            return "";
        }
        if (posicao.equals("reverse landscape direita")) {
            for (int i=0; i < alfabetoBraille.CHAR_ARRAY_SIZE; i++) {
                if (data.equals(alfabetoBraille.chars_Landscape_To_The_Left_direita[i])){
                    return alfabetoBraille.chars_Landscape_To_The_Left_direita[i-1];
                }
            }
            return "";
        }
        return "";
    }

}