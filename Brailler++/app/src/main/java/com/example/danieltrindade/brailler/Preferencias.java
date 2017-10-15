package com.example.danieltrindade.brailler;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;

/**
 * Created by Daniel Trindade on 09/04/2017.
 */

public class Preferencias {

    private static final String TAG = "Brailler++/Preferencias";

    //Context _context;
    MainActivity _main;

    public Preferencias(MainActivity main){
        _main = main;
    }

    public String selecao_preferencias_posicao_telemovel(String data) {
        String posicaoTelemovel = _main.getRotation(_main,false);

        if (posicaoTelemovel.equals("portrait") && data.equals("100000")){
            _main.posicaoPredefinidaDoTelemovel = "portrait esquerda";
            _main.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            _main.rotationBlocked = true;
            return "Success";
        }

        if (posicaoTelemovel.equals("portrait") && data.equals("100")){
            _main.posicaoPredefinidaDoTelemovel = "portrait direita";
            _main.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            _main.rotationBlocked = true;
            return "Success";
        }

        //LandscapeToTheLeftEsquerda - correto
        if (posicaoTelemovel.equals("reverse landscape") && data.equals("100000000")){
            _main.posicaoPredefinidaDoTelemovel = "reverse landscape esquerda";
            _main.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            _main.rotationBlocked = true;
            return "Success";
        }

        //LandscapeToTheLeftDireita - correto
        if (posicaoTelemovel.equals("reverse landscape") && data.equals("001000000")){
            _main.posicaoPredefinidaDoTelemovel = "reverse landscape direita";
            _main.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            _main.rotationBlocked = true;
            return "Success";
        }

        //LandscapeToTheRight
        if (posicaoTelemovel.equals("landscape") && data.equals("1000")){
            _main.posicaoPredefinidaDoTelemovel = "landscape esquerda";
            _main.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            _main.rotationBlocked = true;
            return "Success";
        }

        if (posicaoTelemovel.equals("landscape") && data.equals("100000")){
            _main.posicaoPredefinidaDoTelemovel = "landscape direita";
            _main.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            _main.rotationBlocked = true;
            return "Success";
        }

        else{
            return "Denied";
        }
    }

    public String selecao_preferencias_botoes(String data, String posicaoPredefinidaDoTelemovel, String elemento){
        //posicao portrait
        Log.v(TAG,data);
        Log.v(TAG,posicaoPredefinidaDoTelemovel);
        Log.v(TAG,elemento);
        if (posicaoPredefinidaDoTelemovel.contains("portrait")){
            if (elemento.equals("espaço") && (data.equals("100000000") || data.equals("10000000") || data.equals("1000000"))){
                _main.posicaoEspaco=data;
                return "Success";
            }
            if(elemento.equals("apagar") && (data.equals("100000000") || data.equals("10000000") || data.equals("1000000"))
                    && !data.equals(_main.posicaoEspaco)){
                //Log.d(TAG, (!posicaoApagar.equals("100000000")) ? "true" : "false");
                //Log.d(TAG, (!posicaoEspaco.equals("100000000")) ? "true" : "false");
                //Log.d(TAG, preferencias3 ? "true" : "false");
                _main.posicaoApagar=data;
                if((!_main.posicaoApagar.equals("100000000")) && (!_main.posicaoEspaco.equals("100000000"))){
                    _main.posicaoEnter = "100000000";
                    return "Success";
                }
                if((!_main.posicaoApagar.equals("10000000")) && (!_main.posicaoEspaco.equals("10000000"))){
                    _main.posicaoEnter="10000000";
                    return "Success";
                }
                else{
                    _main.posicaoEnter ="1000000";
                    return "Success";
                }

            }
            else{
                return "Denied";
            }
        }

        //posicao reverse landscape ou landscape
        if (posicaoPredefinidaDoTelemovel.contains("landscape")){
            if (elemento.equals("espaço") && (data.equals("010000000") || data.equals("000010000") || data.equals("000000010"))){
                _main.posicaoEspaco=data;
                return "Success";
            }
            if(elemento.equals("apagar") && (data.equals("010000000") || data.equals("000010000") || data.equals("000000010"))
                    && !data.equals(_main.posicaoEspaco)){
                _main.posicaoApagar=data;
                if(!_main.posicaoApagar.equals("010000000") && !_main.posicaoEspaco.equals("010000000")){
                    _main.posicaoEnter = "010000000";
                    return "Success";
                }
                if(!_main.posicaoApagar.equals("000010000") && !_main.posicaoEspaco.equals("000010000")){
                    _main.posicaoEnter="000010000";
                    return "Success";
                }
                else{
                    _main.posicaoEnter ="000000010";
                    return "Success";
                }
            }
            else{
                return "Denied";
            }
        }

        return "Denied";
    }

}