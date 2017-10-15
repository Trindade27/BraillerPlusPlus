package com.example.danieltrindade.brailler;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.Date;

/**
 * Created by Daniel Trindade on 06/06/2017.
 */

public class OnSwipeTouchListener implements OnTouchListener, OnDoubleTapListener {

    private final GestureDetector gestureDetector;
    MainActivity _main;

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    protected float positionXEvent1Inicio = 0;
    protected float positionYEvent1Inicio = 0;
    protected float positionXEvent2Inicio = 0;
    protected float positionYEvent2Inicio = 0;
    protected float positionXEvent1Fim = 0;
    protected float positionYEvent1Fim = 0;
    protected float positionXEvent2Fim = 0;
    protected float positionYEvent2Fim = 0;
    protected String selecionadoOuNao = "";

    private int numeroClipboardOption = 0;
    public boolean longPress=false;
    public String clipboardOperation = "cancelar";
    public boolean secondFingerDetected = false;

    public String textoCopiado="";

    public OnSwipeTouchListener (Context ctx, MainActivity main){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        _main=main;
    }

    public void init(){
        positionXEvent1Inicio = 0;
        positionYEvent1Inicio = 0;
        positionXEvent2Inicio = 0;
        positionYEvent2Inicio = 0;
        positionXEvent1Fim = 0;
        positionYEvent1Fim = 0;
        positionXEvent2Fim = 0;
        positionYEvent2Fim = 0;
        selecionadoOuNao = "";

        numeroClipboardOption = 0;
        longPress=false;
        clipboardOperation = "cancelar";
        secondFingerDetected = false;

        textoCopiado="";
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        MotionEvent motionEvent1Inicio = event;
        MotionEvent motionEvent1Fim = event;
        MotionEvent motionEvent2Inicio = event;
        MotionEvent motionEvent2Fim = event;

        if (action==MotionEvent.ACTION_DOWN) {
            Log.v(_main.DEBUG_TAG, "Screen pressed");
            return gestureDetector.onTouchEvent(event);

        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {

            secondFingerDetected=true;

            motionEvent1Inicio.setLocation(event.getX(0),event.getY(0));
            positionXEvent1Inicio = motionEvent1Inicio.getX();
            positionYEvent1Inicio = motionEvent1Inicio.getY();

            motionEvent2Inicio.setLocation(event.getX(1), event.getY(1));
            positionXEvent2Inicio = motionEvent2Inicio.getX();
            positionYEvent2Inicio = motionEvent2Inicio.getY();

            Log.v(_main.DEBUG_TAG, "Screen pressed by two fingers");
            return gestureDetector.onTouchEvent(event);

        } else if (action == MotionEvent.ACTION_UP) {

            Log.v(_main.DEBUG_TAG, "one pressed released");
            Log.v(_main.DEBUG_TAG, "secondFingerDetected : " +secondFingerDetected);
            Log.v(_main.DEBUG_TAG, "longPress : " + longPress);
            if(secondFingerDetected && longPress){ //selecionar texto
                if (!clipboardOperation.equals("cancelar")) {
                    processClipboardOperation(clipboardOperation);
                    //longPress = false;
                }
                LogManager.getInstance().logCharEvent("SeleçãoDesativa", new Date(),_main.editFlag);
                secondFingerDetected = false;
                _main.selecaoAtiva = false;
                longPress=false;
                return gestureDetector.onTouchEvent(event);
            }
            else if(!secondFingerDetected && !longPress){ //mover cursor
                return gestureDetector.onTouchEvent(event);
            }
            else if(longPress && !secondFingerDetected){ //só fazer longPress
                LogManager.getInstance().logCharEvent("SeleçãoDesativa", new Date(),_main.editFlag);
                secondFingerDetected = false;
                _main.selecaoAtiva = false;
                longPress=false;
                return gestureDetector.onTouchEvent(event);
            }
            else{ //mover para inicio ou fim
                /*if(!_main.selectedSentence.isEmpty()) { //mover selecao
                    secondFingerDetected = false;
                    longPress=false;
                    _main.selecaoAtiva = true;
                    moveSelectedText(positionXEvent1Inicio, positionYEvent1Inicio, positionXEvent2Inicio, positionYEvent2Inicio,
                            positionXEvent1Fim, positionYEvent1Fim, positionXEvent2Fim, positionYEvent2Fim);
                    _main.selecaoAtiva = false;
                }
                else{
                    secondFingerDetected = false;
                    longPress=false;
                    _main.selecaoAtiva = false;
                    //dont do anything
                }*/
                moveSelectedText(positionXEvent1Inicio, positionYEvent1Inicio, positionXEvent2Inicio, positionYEvent2Inicio,
                        positionXEvent1Fim, positionYEvent1Fim, positionXEvent2Fim, positionYEvent2Fim);
                return false;
            }

        } else if (action == MotionEvent.ACTION_POINTER_UP) {

            motionEvent1Fim.setLocation(event.getX(0), event.getY(0));
            positionXEvent1Fim = motionEvent1Fim.getX();
            positionYEvent1Fim = motionEvent1Fim.getY();

            motionEvent2Fim.setLocation(event.getX(1), event.getY(1));
            positionXEvent2Fim = motionEvent2Fim.getX();
            positionYEvent2Fim = motionEvent2Fim.getY();

            Log.v(_main.DEBUG_TAG, "two finger pressed released");

            if(_main.selecaoAtiva && longPress) {
                selectText(positionXEvent2Inicio, positionYEvent2Inicio, positionXEvent2Fim, positionYEvent2Fim);
            }
            return gestureDetector.onTouchEvent(event);
        } else {
            return gestureDetector.onTouchEvent(event);
        }
    }

    private void processClipboardOperation(String clipboardOperation) {
        if(clipboardOperation.equals("copiar")){ //caso de copiar
            textoCopiado=_main.selectedSentence;
            Log.v(_main.DEBUG_TAG,"texto copiado : " + textoCopiado);

            String avaliarConteudoCopiado = textoCopiado;
            String[] conteudoSemEspacos = avaliarConteudoCopiado.split(" ");
            boolean itContainsOnlySpaces=false;
            boolean itContainsNewLines=false;
            boolean itContainsChar=false;

            if(conteudoSemEspacos.length==0){
                itContainsOnlySpaces=true;
            }
            if(conteudoSemEspacos.length>0){
                for (int i = 0; i < conteudoSemEspacos.length; i++) {
                    if(!conteudoSemEspacos[i].isEmpty()){
                        String[] resultadoTemporario = conteudoSemEspacos[i].split("↵");
                        if(resultadoTemporario.length!=0){
                            itContainsChar = true;
                            break;
                        }
                    }
                }
            }
            if(!itContainsChar && !itContainsOnlySpaces && !textoCopiado.isEmpty()){
                itContainsNewLines=true;
            }

            if (itContainsNewLines) {
                LogManager.getInstance().logCharEvent("Copiar", new Date(),_main.editFlag);
                _main.t1.speak("Linha copiada", TextToSpeech.QUEUE_FLUSH, null);
            } else if (itContainsOnlySpaces) {
                LogManager.getInstance().logCharEvent("Copiar", new Date(),_main.editFlag);
                _main.t1.speak("Espaço copiado", TextToSpeech.QUEUE_FLUSH, null);
            } else if (itContainsChar) {
                LogManager.getInstance().logCharEvent("Copiar", new Date(),_main.editFlag);
                _main.t1.speak("Copiado ", TextToSpeech.QUEUE_FLUSH, null);
                _main.t1.speak(textoCopiado, TextToSpeech.QUEUE_ADD, null);
            }
            else{
                //say nothing
            }

        }
        else if(clipboardOperation.equals("cortar")){ //caso de cortar

            if (_main.actualPositionOfCursor == _main.posicaoInicioSelecao) {
                textoCopiado = "";
                _main.text = _main.text.substring(0, _main.actualPositionOfCursor) +
                        _main.text.substring(_main.actualPositionOfCursor, _main.text.length());
            }
            if (_main.actualPositionOfCursor < _main.posicaoInicioSelecao) {
                textoCopiado = _main.text.substring(_main.actualPositionOfCursor,_main.posicaoInicioSelecao);
                _main.text = _main.text.substring(0, _main.actualPositionOfCursor) +
                        _main.text.substring(_main.posicaoInicioSelecao, _main.text.length());
            }
            if (_main.actualPositionOfCursor > _main.posicaoInicioSelecao) {
                textoCopiado = _main.text.substring(_main.posicaoInicioSelecao,_main.actualPositionOfCursor);
                _main.text = _main.text.substring(0, _main.posicaoInicioSelecao) +
                        _main.text.substring(_main.actualPositionOfCursor, _main.text.length());
                _main.actualPositionOfCursor = _main.posicaoInicioSelecao;
            }

            _main.line = "";
            _main.lastLine = "";

            int incremetador = 0;
            int positionOfWord = 0;

            _main.tv.setText("");
            String textoAColocar = _main.text.replace('↵', '\n');
            _main.tv.append(textoAColocar);
            String[] linhasDisponiveis = textoAColocar.split("\n");

            for (int i = 0; i < linhasDisponiveis.length; i++) {
                if (linhasDisponiveis[i].length() + incremetador + 1 >= _main.actualPositionOfCursor) {
                    _main.line = linhasDisponiveis[i];
                    positionOfWord = _main.actualPositionOfCursor - incremetador;
                    break;
                }
                incremetador = incremetador + linhasDisponiveis[i].length() + 1;
            }

            String[] palavras = _main.line.split(" ");
            incremetador = 0;
            _main.word="";

            for (int i = 0; i < palavras.length; i++) {
                if (palavras[i].length() + incremetador + 1 > positionOfWord) { //vinha com igual
                    if (positionOfWord - incremetador - 1 == -1) {
                        _main.word = "";
                        break;
                    } else {
                        _main.word = palavras[i].substring(0, positionOfWord - incremetador);
                        break;
                    }
                }
                incremetador = incremetador + palavras[i].length() + 1;
            }

            //actualPositionOfCursor = actualPositionOfCursor - 1;
            _main.tv.setSelection(_main.actualPositionOfCursor);

            _main.selectedSentence="";
            _main.moveSelection=false;
            _main.posicaoInicioSelecao = -1;

            Log.v(_main.DEBUG_TAG, "word: " + _main.word);
            Log.v(_main.DEBUG_TAG, "line: " + _main.line);
            Log.v(_main.DEBUG_TAG, "text: " + _main.text);

            Log.v(_main.DEBUG_TAG,"texto cortado : " + textoCopiado);

            String avaliarConteudoApagado = textoCopiado;
            String[] conteudoSemEspacos = avaliarConteudoApagado.split(" ");
            boolean itContainsOnlySpaces=false;
            boolean itContainsNewLines=false;
            boolean itContainsChar=false;

            if(conteudoSemEspacos.length==0){
                itContainsOnlySpaces=true;
            }
            if(conteudoSemEspacos.length>0){
                for (int i = 0; i < conteudoSemEspacos.length; i++) {
                    if(!conteudoSemEspacos[i].isEmpty()){
                        String[] resultadoTemporario = conteudoSemEspacos[i].split("↵");
                        if(resultadoTemporario.length!=0){
                            itContainsChar = true;
                            break;
                        }
                    }
                }
            }

            if(!itContainsChar && !itContainsOnlySpaces && !textoCopiado.isEmpty()){
                itContainsNewLines=true;
            }

            if (itContainsNewLines) {
                LogManager.getInstance().logCharEvent("Cortar", new Date(),_main.editFlag);
                _main.t1.speak("Linha cortada", TextToSpeech.QUEUE_FLUSH, null);
            } else if (itContainsOnlySpaces) {
                LogManager.getInstance().logCharEvent("Cortar", new Date(),_main.editFlag);
                _main.t1.speak("Espaço cortado", TextToSpeech.QUEUE_FLUSH, null);
            } else if(itContainsChar) {
                LogManager.getInstance().logCharEvent("Cortar", new Date(),_main.editFlag);
                _main.t1.speak("Cortado ", TextToSpeech.QUEUE_FLUSH, null);
                _main.t1.speak(textoCopiado, TextToSpeech.QUEUE_ADD, null);
            } else{
                //say nothing
            }


        }
        else if(clipboardOperation.equals("colar")){ //caso de colar

            if (_main.actualPositionOfCursor == _main.posicaoInicioSelecao) {
                _main.text = _main.text.substring(0, _main.actualPositionOfCursor) + textoCopiado +
                        _main.text.substring(_main.actualPositionOfCursor, _main.text.length());
                _main.actualPositionOfCursor = _main.actualPositionOfCursor + textoCopiado.length();
            }
            if (_main.actualPositionOfCursor < _main.posicaoInicioSelecao) {
                _main.text = _main.text.substring(0, _main.actualPositionOfCursor) + textoCopiado +
                        _main.text.substring(_main.posicaoInicioSelecao, _main.text.length());
                _main.actualPositionOfCursor = _main.actualPositionOfCursor + textoCopiado.length();
            }
            if (_main.actualPositionOfCursor > _main.posicaoInicioSelecao) {
                _main.text = _main.text.substring(0, _main.posicaoInicioSelecao) + textoCopiado +
                        _main.text.substring(_main.actualPositionOfCursor, _main.text.length());
                _main.actualPositionOfCursor = _main.posicaoInicioSelecao + textoCopiado.length();
            }

            _main.line = "";
            _main.lastLine = "";

            int incremetador = 0;
            int positionOfWord = 0;

            _main.tv.setText("");
            String textoAColocar = _main.text.replace('↵', '\n');
            _main.tv.append(textoAColocar);
            String[] linhasDisponiveis = textoAColocar.split("\n");

            for (int i = 0; i < linhasDisponiveis.length; i++) {
                if (linhasDisponiveis[i].length() + incremetador + 1 >= _main.actualPositionOfCursor) {
                    _main.line = linhasDisponiveis[i];
                    positionOfWord = _main.actualPositionOfCursor - incremetador;
                    break;
                }
                incremetador = incremetador + linhasDisponiveis[i].length() + 1;
            }

            String[] palavras = _main.line.split(" ");
            incremetador = 0;
            _main.word="";

            for (int i = 0; i < palavras.length; i++) {
                if (palavras[i].length() + incremetador + 1 > positionOfWord) { //vinha com igual
                    if (positionOfWord - incremetador - 1 == -1) {
                        _main.word = "";
                        break;
                    } else {
                        _main.word = palavras[i].substring(0, positionOfWord - incremetador);
                        break;
                    }
                }
                incremetador = incremetador + palavras[i].length() + 1;
            }

            //actualPositionOfCursor = actualPositionOfCursor - 1;
            _main.tv.setSelection(_main.actualPositionOfCursor);

            _main.selectedSentence="";
            _main.moveSelection=false;
            _main.posicaoInicioSelecao = -1;

            Log.v(_main.DEBUG_TAG, "word: " + _main.word);
            Log.v(_main.DEBUG_TAG, "line: " + _main.line);
            Log.v(_main.DEBUG_TAG, "text: " + _main.text);

            Log.v(_main.DEBUG_TAG,"texto cortado : " + textoCopiado);

            String avaliarConteudoApagado = textoCopiado;
            String[] conteudoSemEspacos = avaliarConteudoApagado.split(" ");
            boolean itContainsOnlySpaces=false;
            boolean itContainsNewLines=false;
            boolean itContainsChar=false;

            if(conteudoSemEspacos.length==0){
                itContainsOnlySpaces=true;
            }
            if(conteudoSemEspacos.length>0){
                for (int i = 0; i < conteudoSemEspacos.length; i++) {
                    if(!conteudoSemEspacos[i].isEmpty()){
                        String[] resultadoTemporario = conteudoSemEspacos[i].split("↵");
                        if(resultadoTemporario.length!=0){
                            itContainsChar = true;
                            break;
                        }
                    }
                }
            }
            if(!itContainsChar && !itContainsOnlySpaces && !textoCopiado.isEmpty()){
                itContainsNewLines=true;
            }

            if (itContainsNewLines) {
                LogManager.getInstance().logCharEvent("Colar", new Date(),_main.editFlag);
                _main.t1.speak("Linha colada", TextToSpeech.QUEUE_FLUSH, null);
            } else if (itContainsOnlySpaces) {
                LogManager.getInstance().logCharEvent("Colar", new Date(),_main.editFlag);
                _main.t1.speak("Espaço colado", TextToSpeech.QUEUE_FLUSH, null);
            } else if (itContainsChar){
                LogManager.getInstance().logCharEvent("Colar", new Date(),_main.editFlag);
                _main.t1.speak("Colado ", TextToSpeech.QUEUE_FLUSH, null);
                _main.t1.speak(textoCopiado, TextToSpeech.QUEUE_ADD, null);
            }
            else{
                //nothing to say
            }
        }
        else if(clipboardOperation.equals("ouvirSelecao")){
            Log.v(_main.DEBUG_TAG,"texto selecionado aqui : " + _main.selectedSentence);
            LogManager.getInstance().logCharEvent("OuvirSeleção", new Date(),_main.editFlag);
            _main.t1.speak("Texto selecionado ", TextToSpeech.QUEUE_FLUSH, null);
            _main.t1.speak(_main.selectedSentence, TextToSpeech.QUEUE_ADD, null);
        }
        else if(clipboardOperation.equals("selecionarTudo")) {
            _main.tv.setSelection(0,_main.text.length());
            _main.posicaoInicioSelecao=0;
            _main.actualPositionOfCursor = _main.text.length();
            _main.selectedSentence = _main.text;
            _main.selecaoAtiva=false;
            LogManager.getInstance().logCharEvent("SelecionarTudo", new Date(),_main.editFlag);
            _main.t1.speak("Texto selecionado", TextToSpeech.QUEUE_FLUSH, null);
            _main.t1.speak(_main.text, TextToSpeech.QUEUE_ADD, null);
        }
        else{
            Log.v(_main.DEBUG_TAG,"nothing to say here :/");
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }


    //@Override
    public boolean selectText(float e1X, float e1Y, float e2X, float e2Y) {
        boolean result = false;
        float diffY = e2Y - e1Y;
        float diffX = e2X - e1X;
        if (Math.abs(diffX) > Math.abs(diffY) && diffX > 0) {
            onSwipeRight();
        } else if (Math.abs(diffX) > Math.abs(diffY) && diffX < 0) {
            onSwipeLeft();
        } else if (Math.abs(diffY) > Math.abs(diffX) && diffY > 0) {
            Log.v(_main.DEBUG_TAG,"ko");
            onSwipeBottom();
        } else if (Math.abs(diffY) > Math.abs(diffX) && diffY < 0) {
            onSwipeTop();
        } else {
            //dont do anything
        }

        return result;
    }

    private boolean moveSelectedText(float positionXEvent1Inicio, float positionYEvent1Inicio, float positionXEvent2Inicio, float positionYEvent2Inicio, float positionXEvent1Fim, float positionYEvent1Fim, float positionXEvent2Fim, float positionYEvent2Fim) {
        boolean result = false;
        float diffE1Y = positionYEvent1Fim - positionYEvent1Inicio;
        float diffE1X = positionXEvent1Fim - positionXEvent1Inicio;
        float diffE2Y = positionYEvent2Fim - positionYEvent2Inicio;
        float diffE2X = positionXEvent2Fim - positionXEvent2Inicio;
        if (Math.abs(diffE1X) > Math.abs(diffE1Y) && Math.abs(diffE2X) > Math.abs(diffE2Y) && diffE1X > 0 && diffE2X > 0) {
            //_main.moveSelection = true;
            onSwipeRight();
        } else if (Math.abs(diffE1X) > Math.abs(diffE1Y) && Math.abs(diffE2X) > Math.abs(diffE2Y) && diffE1X < 0 && diffE2X < 0) {
            //_main.moveSelection = true;
            onSwipeLeft();
        } else if (Math.abs(diffE1Y) > Math.abs(diffE1X) && Math.abs(diffE2Y) > Math.abs(diffE2X) && diffE1Y > 0 && diffE2Y > 0) {
            //_main.moveSelection = true;
            onSwipeBottom();
        } else if (Math.abs(diffE1Y) > Math.abs(diffE1X) && Math.abs(diffE2Y) > Math.abs(diffE2X) && diffE1Y < 0 && diffE2Y < 0) {
            //_main.moveSelection = true;
            onSwipeTop();
        } else {
            secondFingerDetected = false;
            longPress=false;
            _main.selecaoAtiva = false;
        }
        return result;

    }


    public final class GestureListener extends SimpleOnGestureListener implements OnDoubleTapListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        //update - maybe not necessary
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            LogManager.getInstance().logCharEvent("DuploClique", new Date(),_main.editFlag);
            _main.t1.speak(_main.text, TextToSpeech.QUEUE_FLUSH, null);
            Log.v(_main.DEBUG_TAG, "duplo clique");
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e){
            if (_main.editFlag){
                LogManager.getInstance().logCharEvent("SeleçãoAtiva", new Date(),_main.editFlag);
                longPress=true;
                clipboardOperation="cancelar";
                numeroClipboardOption=0;
                _main.selecaoAtiva=true;
                _main.moveSelection=false;
                if (_main.posicaoInicioSelecao==0 && _main.selectedSentence.equals("")){
                    _main.posicaoInicioSelecao=0;
                    _main.t1.speak("Início da seleção de texto", TextToSpeech.QUEUE_FLUSH, null);
                }
                else if(_main.posicaoInicioSelecao<0){
                    _main.posicaoInicioSelecao=_main.actualPositionOfCursor;
                    _main.t1.speak("Início da seleção de texto", TextToSpeech.QUEUE_FLUSH, null);
                }
                else {
                    _main.t1.speak("Início da seleção de texto", TextToSpeech.QUEUE_FLUSH, null);
                    _main.t1.speak(_main.selectedSentence, TextToSpeech.QUEUE_ADD, null);
                }
            }
            Log.v(_main.DEBUG_TAG,"Long press activated");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.v(_main.DEBUG_TAG, "velocityX : " + velocityX);
            Log.v(_main.DEBUG_TAG, "velocityY : " + velocityY);
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

    }

    public void onSwipeRight() {
        if (_main.editFlag) {

            Log.v(_main.DEBUG_TAG,"_main.selecaoAtiva : " +_main.selecaoAtiva);
            Log.v(_main.DEBUG_TAG,"_main.moveSelection : " +_main.moveSelection);

            //movimento do cursor
            if (!_main.selecaoAtiva && !secondFingerDetected) {
                //carateres
                if(_main.edicaoCarateres){

                    _main.posicaoInicioSelecao = -1;
                    _main.selectedSentence = "";
                    _main.moveSelection=false;
                    if (_main.actualPositionOfCursor == _main.tv.getText().length()) {
                        _main.t1.speak("Fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                        _main.tv.setSelection(_main.actualPositionOfCursor);
                    } else {
                        String carater = _main.text.substring(_main.actualPositionOfCursor, _main.actualPositionOfCursor + 1);
                        _main.actualPositionOfCursor = _main.actualPositionOfCursor + 1;
                        _main.tv.setSelection(_main.actualPositionOfCursor);
                        if (carater.equals(" ")) {
                            _main.t1.speak("Espaço", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (carater.equals("↵")) {
                            _main.t1.speak("Nova linha", TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            _main.t1.speak(carater, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }

                }

                //palavras
                if (_main.edicaoPalavras) {
                    _main.posicaoInicioSelecao = -1;
                    _main.selectedSentence = "";
                    _main.moveSelection = false;
                    if (_main.actualPositionOfCursor == _main.text.length()) {
                        _main.t1.speak("fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                        _main.tv.setSelection(_main.actualPositionOfCursor);
                    } else {

                        String textoAanalisar = _main.text.substring(_main.actualPositionOfCursor,_main.text.length());
                        int posInicio = 0;
                        while(textoAanalisar.substring(posInicio,posInicio+1).contains(" ")
                                || textoAanalisar.substring(posInicio,posInicio+1).equals("↵")){
                            posInicio++;
                            if(_main.actualPositionOfCursor+posInicio==_main.text.length()){
                                break;
                            }
                        }

                        if (_main.actualPositionOfCursor+posInicio == _main.text.length()) {
                            _main.actualPositionOfCursor = _main.actualPositionOfCursor+posInicio;
                            _main.t1.speak("Fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                            _main.tv.setSelection(_main.actualPositionOfCursor);
                        }else {

                            int posicaoPalavra = posInicio;

                            while (textoAanalisar.substring(posicaoPalavra, posicaoPalavra+1).matches("[a-zA-Zçáéíóúàèìùâêôãõ]+")) {
                                posicaoPalavra++;
                                if (_main.actualPositionOfCursor+posicaoPalavra == _main.text.length()) {
                                    break;
                                }
                            }

                            _main.word = textoAanalisar.substring(posInicio,posicaoPalavra);
                            _main.actualPositionOfCursor = _main.actualPositionOfCursor + posicaoPalavra;

                            if (_main.actualPositionOfCursor==_main.text.length()) {
                                _main.actualPositionOfCursor = _main.text.length();
                                _main.tv.setSelection(_main.text.length());
                                _main.t1.speak(_main.word, TextToSpeech.QUEUE_FLUSH, null);
                            } else {
                                _main.tv.setSelection(_main.actualPositionOfCursor);
                                _main.t1.speak(_main.word, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }

                    }

                }

                LogManager.getInstance().logCharEvent("MovimentoCursorDireita", new Date(),_main.editFlag);
                Log.v(_main.DEBUG_TAG, "Movimento do cursor para a direita");
            }
            //modo de seleção
            else if(_main.selecaoAtiva && !_main.moveSelection) {
                if(_main.edicaoCarateres){
                    if (_main.actualPositionOfCursor == _main.tv.getText().length()) {
                        _main.t1.speak("Fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        String carater = _main.text.substring(_main.actualPositionOfCursor, _main.actualPositionOfCursor + 1);
                        _main.actualPositionOfCursor = _main.actualPositionOfCursor + 1;
                        if (_main.posicaoInicioSelecao < _main.actualPositionOfCursor) {
                            _main.selectedSentence = _main.selectedSentence + _main.text.substring(_main.actualPositionOfCursor - 1, _main.actualPositionOfCursor);
                            _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            selecionadoOuNao = "Selecionado";
                        }
                        if (_main.posicaoInicioSelecao > _main.actualPositionOfCursor) {
                            _main.selectedSentence = _main.text.substring(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                            _main.tv.setSelection(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                            selecionadoOuNao = "Desselecionado";
                        }
                        if (_main.posicaoInicioSelecao == _main.actualPositionOfCursor) {
                            _main.selectedSentence = "";
                            _main.moveSelection = false;
                            _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            selecionadoOuNao = "Desselecionado";
                        }

                        if (carater.equals(" ")) {
                            _main.t1.speak("Espaço", TextToSpeech.QUEUE_FLUSH, null);
                            _main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                        } else if (carater.equals("↵")) {
                            _main.t1.speak("Nova linha", TextToSpeech.QUEUE_FLUSH, null);
                            if (selecionadoOuNao.equals("Selecionado")) {
                                selecionadoOuNao = "Selecionada";
                            }
                            if (selecionadoOuNao.equals("Desselecionado")) {
                                selecionadoOuNao = "Desselecionada";
                            }
                            _main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                        } else {
                            _main.t1.speak(carater, TextToSpeech.QUEUE_FLUSH, null);
                            _main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                }
                if(_main.edicaoPalavras){
                    if (_main.actualPositionOfCursor == _main.tv.getText().length()) {
                        _main.t1.speak("Fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        String textoAanalisar="";
                        if(_main.actualPositionOfCursor<_main.posicaoInicioSelecao) {
                            textoAanalisar = _main.text.substring(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                            int posInicio = 0;
                            while (textoAanalisar.substring(posInicio, posInicio + 1).contains(" ")
                                    || textoAanalisar.substring(posInicio, posInicio + 1).equals("↵")) {
                                posInicio++;
                                if (_main.actualPositionOfCursor + posInicio == _main.posicaoInicioSelecao) {
                                    break;
                                }
                            }

                            if (_main.actualPositionOfCursor + posInicio == _main.text.length()) {
                                selecionadoOuNao = "";
                                _main.actualPositionOfCursor = _main.actualPositionOfCursor + posInicio;
                                _main.selectedSentence = _main.text.substring(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                                _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                                //_main.t1.speak("Fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                                //_main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                                _main.word="Fim do texto";
                            } else {

                                int posicaoPalavra = 0;
                                boolean fimDoTexto = false;
                                if (posInicio == 0) {
                                    posicaoPalavra = 0;
                                } else {
                                    posicaoPalavra = posInicio;
                                }


                                if(textoAanalisar.length()==posicaoPalavra) {
                                    _main.actualPositionOfCursor = _main.actualPositionOfCursor + textoAanalisar.length();
                                    textoAanalisar = _main.text.substring(_main.actualPositionOfCursor, _main.text.length());
                                    if (textoAanalisar.equals("")) {
                                        //selecionadoOuNao = "Desselecionado";
                                        _main.selectedSentence = "";
                                        _main.tv.setSelection(_main.actualPositionOfCursor);
                                        //_main.t1.speak("Fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                                    }else{
                                        posInicio = 0;
                                        while (textoAanalisar.substring(posInicio, posInicio + 1).contains(" ")
                                                || textoAanalisar.substring(posInicio, posInicio + 1).equals("↵")) {
                                            posInicio++;
                                            if (_main.actualPositionOfCursor + posInicio >= _main.text.length()) {
                                                break;
                                            }
                                        }
                                        posicaoPalavra = posInicio;
                                        if(posicaoPalavra+_main.actualPositionOfCursor==_main.text.length()){
                                            fimDoTexto=true;
                                            selecionadoOuNao = "";
                                            _main.actualPositionOfCursor = _main.text.length();
                                            _main.selectedSentence = _main.text.substring(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                                            _main.word = "Fim do texto";
                                            _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                                        }
                                        else {
                                            while (textoAanalisar.substring(posicaoPalavra, posicaoPalavra + 1).matches("[a-zA-Zçáéíóúàèìùâêôãõ]+")) {
                                                posicaoPalavra++;
                                                if (_main.actualPositionOfCursor + posicaoPalavra == _main.text.length()) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                else {
                                    while (textoAanalisar.substring(posicaoPalavra, posicaoPalavra + 1).matches("[a-zA-Zçáéíóúàèìùâêôãõ]+")) {
                                        posicaoPalavra++;
                                        if (_main.actualPositionOfCursor + posicaoPalavra == _main.posicaoInicioSelecao) {
                                            break;
                                        }
                                    }
                                }

                                if(!fimDoTexto) {
                                    _main.word = textoAanalisar.substring(posInicio, posicaoPalavra);
                                    _main.actualPositionOfCursor = _main.actualPositionOfCursor + posicaoPalavra;
                                }else{
                                    fimDoTexto=!fimDoTexto;
                                }
                            }
                        }
                        else {
                            textoAanalisar = _main.text.substring(_main.actualPositionOfCursor, _main.text.length());
                            int posInicio = 0;
                            while (textoAanalisar.substring(posInicio, posInicio + 1).contains(" ")
                                    || textoAanalisar.substring(posInicio, posInicio + 1).equals("↵")) {
                                posInicio++;
                                if (_main.actualPositionOfCursor + posInicio == _main.text.length()) {
                                    break;
                                }
                            }

                            if (_main.actualPositionOfCursor + posInicio == _main.text.length()) {
                                //selecionadoOuNao = "Selecionado";
                                _main.actualPositionOfCursor = _main.actualPositionOfCursor + posInicio;
                                _main.selectedSentence = _main.text.substring(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                                _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                                _main.word = "Fim do texto";
                            } else {

                                int posicaoPalavra = 0;
                                if (posInicio == 0) {
                                    posicaoPalavra = 0;
                                } else {
                                    posicaoPalavra = posInicio;
                                }

                                while (textoAanalisar.substring(posicaoPalavra, posicaoPalavra + 1).matches("[a-zA-Zçáéíóúàèìùâêôãõ]+")) {
                                    posicaoPalavra++;
                                    if (_main.actualPositionOfCursor + posicaoPalavra == _main.text.length()) {
                                        break;
                                    }
                                }

                                _main.word = textoAanalisar.substring(posInicio, posicaoPalavra);
                                _main.actualPositionOfCursor = _main.actualPositionOfCursor + posicaoPalavra;
                            }
                        }

                        if (_main.posicaoInicioSelecao < _main.actualPositionOfCursor) {
                            _main.selectedSentence = _main.text.substring(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            if(_main.word.equals("Fim do texto")){
                                selecionadoOuNao="";
                            }
                            else{
                                selecionadoOuNao = "Selecionado";}
                        }
                        if (_main.posicaoInicioSelecao > _main.actualPositionOfCursor) {
                            _main.selectedSentence = _main.text.substring(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                            _main.tv.setSelection(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                            if(_main.word.equals("Fim do texto")){
                                selecionadoOuNao="";
                            }
                            else{
                                selecionadoOuNao = "Desselecionado";}
                        }
                        if (_main.posicaoInicioSelecao == _main.actualPositionOfCursor) {
                            _main.selectedSentence = "";
                            _main.moveSelection = false;
                            _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            selecionadoOuNao = "Desselecionado";
                        }

                        _main.t1.speak(_main.word, TextToSpeech.QUEUE_FLUSH, null);
                        _main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);

                    }

                }

                LogManager.getInstance().logCharEvent("MovimentoCursorDireita", new Date(),_main.editFlag);
                Log.v(_main.DEBUG_TAG, "Seleção para a direita");
                Log.v(_main.DEBUG_TAG, "Texto Selecionado : " + _main.selectedSentence);
            }

            //mover a selecao
            else if(!_main.selecaoAtiva && secondFingerDetected){
                _main.tv.setSelection(_main.text.length());
                _main.actualPositionOfCursor = _main.text.length();
                //_main.posicaoInicioSelecao = _main.text.length();
                _main.selectedSentence="";
                _main.t1.speak("Fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                _main.moveSelection = false;
                secondFingerDetected = false;
                longPress=false;
                _main.selecaoAtiva = false;
                LogManager.getInstance().logCharEvent("MovimentoFimTexto", new Date(),_main.editFlag);
                Log.v(_main.DEBUG_TAG, "Cursor no fim do texto");
            }

            else {
                LogManager.getInstance().logCharEvent("MovimentoParaDireita", new Date(),_main.editFlag);
                Log.v(_main.DEBUG_TAG, "Nada para fazer no OnSwipeRight");
                //dont do anything
            }

        }
        else{
            LogManager.getInstance().logCharEvent("MovimentoParaDireita", new Date(),_main.editFlag);
        }
    }

    public void onSwipeLeft() {
        if (_main.editFlag) {

            //movimento do cursor
            if (!_main.selecaoAtiva && !secondFingerDetected) {
                //carateres
                if (_main.edicaoCarateres) {
                    _main.posicaoInicioSelecao = -1;
                    _main.selectedSentence = "";
                    _main.moveSelection = false;
                    if (_main.actualPositionOfCursor == 0) {
                        _main.t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                        _main.tv.setSelection(_main.actualPositionOfCursor);
                    } else {
                        String carater = _main.text.substring(_main.actualPositionOfCursor - 1, _main.actualPositionOfCursor);
                        _main.actualPositionOfCursor = _main.actualPositionOfCursor - 1;
                        _main.tv.setSelection(_main.actualPositionOfCursor);
                        if (carater.equals(" ")) {
                            _main.t1.speak("Espaço", TextToSpeech.QUEUE_FLUSH, null);
                        } else if (carater.equals("↵")) {
                            _main.t1.speak("Nova linha", TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            _main.t1.speak(carater, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }

                }

                //palavras
                if (_main.edicaoPalavras) {
                    _main.posicaoInicioSelecao = -1;
                    _main.selectedSentence = "";
                    _main.moveSelection = false;
                    if (_main.actualPositionOfCursor == 0) {
                        _main.t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                        _main.tv.setSelection(_main.actualPositionOfCursor);
                    } else {
                        String textoAanalisar = _main.text.substring(0,_main.actualPositionOfCursor);
                        while(textoAanalisar.substring(_main.actualPositionOfCursor-1,_main.actualPositionOfCursor).equals(" ")
                                || textoAanalisar.substring(_main.actualPositionOfCursor-1,_main.actualPositionOfCursor).equals("↵")){
                            _main.actualPositionOfCursor--;
                            if(_main.actualPositionOfCursor==0){
                                break;
                            }
                        }

                        if (_main.actualPositionOfCursor == 0) {
                            _main.t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                            _main.tv.setSelection(_main.actualPositionOfCursor);
                        }else {

                            int posicaoPalavra = _main.actualPositionOfCursor;

                            while (textoAanalisar.substring(posicaoPalavra - 1, posicaoPalavra).matches("[a-zA-Zçáéíóúàèìùâêôãõ]+")) {
                                posicaoPalavra--;
                                if (posicaoPalavra == 0) {
                                    break;
                                }
                            }

                            _main.word = textoAanalisar.substring(posicaoPalavra, _main.actualPositionOfCursor);
                            _main.actualPositionOfCursor = posicaoPalavra;

                            if (_main.actualPositionOfCursor <= 0) {
                                _main.actualPositionOfCursor = 0;
                                _main.tv.setSelection(0);
                                _main.t1.speak(_main.word, TextToSpeech.QUEUE_FLUSH, null);
                            } else {
                                _main.tv.setSelection(_main.actualPositionOfCursor);
                                _main.t1.speak(_main.word, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }

                    }

                }
                LogManager.getInstance().logCharEvent("MovimentoCursorEsquerda", new Date(),_main.editFlag);
                Log.v(_main.DEBUG_TAG, "Movimento do cursor para a esquerda");

            }

            //modo de seleção
            else if(_main.selecaoAtiva && !_main.moveSelection) {
                if(_main.edicaoCarateres){
                    if (_main.actualPositionOfCursor == 0) {
                        _main.t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        String carater = _main.text.substring(_main.actualPositionOfCursor - 1, _main.actualPositionOfCursor);
                        _main.actualPositionOfCursor = _main.actualPositionOfCursor - 1;
                        if (_main.posicaoInicioSelecao > _main.actualPositionOfCursor) {
                            _main.selectedSentence = _main.text.substring(_main.actualPositionOfCursor, _main.actualPositionOfCursor + 1) + _main.selectedSentence;
                            _main.tv.setSelection(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                            selecionadoOuNao = "Selecionado";
                        }
                        if (_main.posicaoInicioSelecao < _main.actualPositionOfCursor) {
                            _main.selectedSentence = _main.text.substring(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            selecionadoOuNao = "Desselecionado";
                        }
                        if (_main.posicaoInicioSelecao == _main.actualPositionOfCursor) {
                            _main.selectedSentence = "";
                            _main.moveSelection = false;
                            _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            selecionadoOuNao = "Desselecionado";
                        }

                        if (carater.equals(" ")) {
                            _main.t1.speak("Espaço", TextToSpeech.QUEUE_FLUSH, null);
                            _main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                        } else if (carater.equals("↵")) {
                            _main.t1.speak("Nova linha", TextToSpeech.QUEUE_FLUSH, null);
                            if (selecionadoOuNao.equals("Selecionado")) {
                                selecionadoOuNao = "Selecionada";
                            }
                            if (selecionadoOuNao.equals("Desselecionado")) {
                                selecionadoOuNao = "Desselecionada";
                            }
                            _main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                        } else {
                            _main.t1.speak(carater, TextToSpeech.QUEUE_FLUSH, null);
                            _main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                }
                if(_main.edicaoPalavras){
                    if (_main.actualPositionOfCursor == 0) {
                        _main.t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                    } else {

                        String textoAanalisar ="";
                        int espacoVazios=0;

                        if(_main.actualPositionOfCursor>_main.posicaoInicioSelecao) {
                            textoAanalisar = _main.text.substring(_main.posicaoInicioSelecao,_main.actualPositionOfCursor);
                            //_main.actualPositionOfCursor = _main.posicaoInicioSelecao;
                            int ultimaPos = _main.actualPositionOfCursor-_main.posicaoInicioSelecao;
                            //int original = _main.actualPositionOfCursor;
                            //String textoAanalisar = _main.text.substring(0, _main.actualPositionOfCursor);
                            while (textoAanalisar.substring(ultimaPos - 1, ultimaPos).equals(" ")
                                    || textoAanalisar.substring(ultimaPos - 1, ultimaPos).equals("↵")) {
                                espacoVazios++;
                                ultimaPos--;
                                if (ultimaPos == 0) {
                                    break;
                                }
                            }

                            if (_main.actualPositionOfCursor-ultimaPos == 0) {
                                selecionadoOuNao = "";
                                _main.actualPositionOfCursor = _main.actualPositionOfCursor - ultimaPos;
                                _main.selectedSentence = _main.text.substring(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                                _main.word = "Início do texto";
                                _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                                //_main.t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                                //_main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                            } else {

                                int posicaoPalavra = ultimaPos;
                                boolean inicioDoTexto = false;

                                if(posicaoPalavra==0){
                                    _main.actualPositionOfCursor = _main.actualPositionOfCursor - textoAanalisar.length();
                                    textoAanalisar = _main.text.substring(0, _main.actualPositionOfCursor);
                                    if (textoAanalisar.equals("")) {
                                        //selecionadoOuNao = "Desselecionado";
                                        _main.selectedSentence = "";
                                        _main.tv.setSelection(_main.actualPositionOfCursor);
                                        //_main.t1.speak("Fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                                    }else{
                                        espacoVazios=0;
                                        ultimaPos = _main.actualPositionOfCursor;
                                        while (textoAanalisar.substring(ultimaPos-1, ultimaPos).contains(" ")
                                                || textoAanalisar.substring(ultimaPos-1, ultimaPos).equals("↵")) {
                                            ultimaPos--;
                                            espacoVazios++;
                                            if (ultimaPos == 0) {
                                                break;
                                            }
                                        }
                                        posicaoPalavra = ultimaPos;

                                        if(posicaoPalavra==0){
                                            inicioDoTexto=true;
                                            selecionadoOuNao = "";
                                            _main.actualPositionOfCursor = 0;
                                            _main.selectedSentence = _main.text.substring(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                                            _main.word = "Início do texto";
                                            _main.tv.setSelection(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                                        }
                                        else {
                                            while (textoAanalisar.substring(posicaoPalavra - 1, posicaoPalavra).matches("[a-zA-Zçáéíóúàèìùâêôãõ]+")) {
                                                posicaoPalavra--;
                                                if (posicaoPalavra == 0) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                else {
                                    while (textoAanalisar.substring(posicaoPalavra - 1, posicaoPalavra).matches("[a-zA-Zçáéíóúàèìùâêôãõ]+")) {
                                        posicaoPalavra--;
                                        if (posicaoPalavra == 0) {
                                            break;
                                        }
                                    }
                                }

                                if(!inicioDoTexto) {
                                    _main.word = textoAanalisar.substring(posicaoPalavra, ultimaPos);
                                    _main.actualPositionOfCursor = _main.actualPositionOfCursor - (ultimaPos - posicaoPalavra) - espacoVazios;
                                }
                                else{
                                    inicioDoTexto=!inicioDoTexto;
                                }
                            }
                        }

                        else{
                            textoAanalisar = _main.text.substring(0, _main.actualPositionOfCursor);

                            int original2 = _main.actualPositionOfCursor;
                            //String textoAanalisar = _main.text.substring(0, _main.actualPositionOfCursor);
                            while (textoAanalisar.substring(_main.actualPositionOfCursor - 1, _main.actualPositionOfCursor).equals(" ")
                                    || textoAanalisar.substring(_main.actualPositionOfCursor - 1, _main.actualPositionOfCursor).equals("↵")) {
                                _main.actualPositionOfCursor--;
                                if (_main.actualPositionOfCursor == 0) {
                                    break;
                                }
                            }

                            if (_main.actualPositionOfCursor == 0) {
                                selecionadoOuNao = "";
                                _main.selectedSentence = _main.text.substring(_main.actualPositionOfCursor,_main.posicaoInicioSelecao);
                                _main.tv.setSelection(_main.actualPositionOfCursor,_main.posicaoInicioSelecao);
                                _main.word = "Início do texto";
                                //_main.t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                                //_main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                            } else {

                                int posicaoPalavra = 0;
                                if (_main.actualPositionOfCursor == original2) {
                                    posicaoPalavra = _main.actualPositionOfCursor;
                                } else {
                                    posicaoPalavra = _main.actualPositionOfCursor;
                                }

                                while (textoAanalisar.substring(posicaoPalavra - 1, posicaoPalavra).matches("[a-zA-Zçáéíóúàèìùâêôãõ]+")) {
                                    posicaoPalavra--;
                                    if (posicaoPalavra == 0) {
                                        break;
                                    }
                                }

                                _main.word = textoAanalisar.substring(posicaoPalavra, _main.actualPositionOfCursor);
                                _main.actualPositionOfCursor = posicaoPalavra;
                            }
                        }
                        if (_main.posicaoInicioSelecao > _main.actualPositionOfCursor) {
                            _main.selectedSentence = _main.text.substring(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                            _main.tv.setSelection(_main.actualPositionOfCursor, _main.posicaoInicioSelecao);
                            if(_main.word.equals("Início do texto")){
                                selecionadoOuNao="";
                            }
                            else{
                                selecionadoOuNao = "Selecionado";}
                        }
                        if (_main.posicaoInicioSelecao < _main.actualPositionOfCursor) {
                            _main.selectedSentence = _main.text.substring(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            if(_main.word.equals("Início do texto")){
                                selecionadoOuNao="";
                            }
                            else{
                                selecionadoOuNao = "Desselecionado";}
                        }
                        if (_main.posicaoInicioSelecao == _main.actualPositionOfCursor) {
                            _main.selectedSentence = "";
                            _main.moveSelection = false;
                            _main.tv.setSelection(_main.posicaoInicioSelecao, _main.actualPositionOfCursor);
                            selecionadoOuNao = "Desselecionado";
                        }
                        _main.t1.speak(_main.word, TextToSpeech.QUEUE_FLUSH, null);
                        _main.t1.speak(selecionadoOuNao, TextToSpeech.QUEUE_ADD, null);
                    }
                }
                LogManager.getInstance().logCharEvent("MovimentoCursorEsquerda", new Date(),_main.editFlag);
                Log.v(_main.DEBUG_TAG,"Seleção para a esquerda");
                Log.v(_main.DEBUG_TAG,"Texto Selecionado : " + _main.selectedSentence);
            }

            //mover a selecao
            else if(!_main.selecaoAtiva && secondFingerDetected){
                _main.tv.setSelection(0);
                _main.actualPositionOfCursor = 0;
                //_main.posicaoInicioSelecao = 0;
                _main.selectedSentence="";
                _main.moveSelection = false;
                _main.t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                secondFingerDetected = false;
                longPress=false;
                _main.selecaoAtiva = false;
                /*if (_main.actualPositionOfCursor==0 || _main.posicaoInicioSelecao==0){
                    _main.t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                }
                else{
                    //String carater = _main.text.substring(_main.actualPositionOfCursor - 1, _main.actualPositionOfCursor);
                    //_main.actualPositionOfCursor = _main.actualPositionOfCursor-1;
                    if(_main.posicaoInicioSelecao>_main.actualPositionOfCursor){
                        _main.actualPositionOfCursor= _main.actualPositionOfCursor-1;
                        _main.posicaoInicioSelecao=_main.posicaoInicioSelecao-1;
                        _main.text=_main.text.substring(0,_main.actualPositionOfCursor) + _main.selectedSentence +
                                    _main.text.substring(_main.posicaoInicioSelecao,_main.text.length());
                        _main.tv.setSelection(_main.actualPositionOfCursor,_main.posicaoInicioSelecao);
                    }
                    if(_main.posicaoInicioSelecao<_main.actualPositionOfCursor){
                        _main.selectedSentence = _main.text.substring(_main.posicaoInicioSelecao,_main.actualPositionOfCursor);
                        _main.tv.setSelection(_main.posicaoInicioSelecao,_main.actualPositionOfCursor);
                        selecionadoOuNao = "Desselecionado";
                    }
                    /*if(_main.posicaoInicioSelecao==_main.actualPositionOfCursor){
                        _main.selectedSentence = "";
                        _main.moveSelection=false;
                        _main.tv.setSelection(_main.posicaoInicioSelecao,_main.actualPositionOfCursor);
                        selecionadoOuNao = "Desselecionado";
                    }
                }*/
                LogManager.getInstance().logCharEvent("MovimentoInicioTexto", new Date(),_main.editFlag);
                Log.v(_main.DEBUG_TAG, "Cursor no início do texto");
            }

            else {
                LogManager.getInstance().logCharEvent("MovimentoParaEsquerda", new Date(),_main.editFlag);
                Log.v(_main.DEBUG_TAG, "Nada para fazer no OnSwipeLeft");
                //dont do anything
            }

        }
        else{
            LogManager.getInstance().logCharEvent("MovimentoParaEsquerda", new Date(),_main.editFlag);
        }
    }

    public void onSwipeTop() {
        if(_main.editFlag){

            //obter opções de clipboard
            if (_main.selecaoAtiva  && !_main.moveSelection) {
                if (_main.actualPositionOfCursor != _main.posicaoInicioSelecao) {
                    numeroClipboardOption++;
                    if (numeroClipboardOption == 1) {
                        clipboardOperation = "copiar";
                        _main.t1.speak("Copiar", TextToSpeech.QUEUE_FLUSH, null);
                    } else if (numeroClipboardOption == 2) {
                        clipboardOperation = "cortar";
                        _main.t1.speak("Cortar", TextToSpeech.QUEUE_FLUSH, null);
                    } else if (numeroClipboardOption == 3) {
                        if (!textoCopiado.isEmpty()) {
                            clipboardOperation = "colar";
                            _main.t1.speak("Colar", TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            numeroClipboardOption=4;
                            clipboardOperation = "ouvirSelecao";
                            _main.t1.speak("Ouvir a seleção", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }else if(numeroClipboardOption == 4){
                        clipboardOperation = "ouvirSelecao";
                        _main.t1.speak("Ouvir a seleção", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else if(numeroClipboardOption == 5) {
                        clipboardOperation = "selecionarTudo";
                        _main.t1.speak("Selecionar Tudo", TextToSpeech.QUEUE_FLUSH, null);
                    }else{
                        numeroClipboardOption = 0;
                        clipboardOperation = "cancelar";
                        _main.t1.speak("Cancelar", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                else {
                    if (!textoCopiado.isEmpty()){
                        if (clipboardOperation.equals("cancelar")) {
                            clipboardOperation = "colar";
                            _main.t1.speak("Colar", TextToSpeech.QUEUE_FLUSH, null);
                        }else if(clipboardOperation.equals("colar")) {
                            clipboardOperation = "selecionarTudo";
                            _main.t1.speak("Selecionar Tudo", TextToSpeech.QUEUE_FLUSH, null);
                        }else{
                            clipboardOperation = "cancelar";
                            _main.t1.speak("Cancelar", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                    else{
                        if (!clipboardOperation.equals("cancelar")) {
                            clipboardOperation = "cancelar";
                            _main.t1.speak("Cancelar", TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            clipboardOperation = "selecionarTudo";
                            _main.t1.speak("Selecionar Tudo", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }

                }
            }
            //mover a selecao
            else if(_main.selecaoAtiva && _main.moveSelection){
                Log.v(_main.DEBUG_TAG, "Seleção movimentada para a cima");
            }
            else{
                //dont do anything
                Log.v(_main.DEBUG_TAG, "Nada para fazer no OnSwipeTop");
            }
            LogManager.getInstance().logCharEvent("MovimentoParaCima", new Date(),_main.editFlag);
        }
        else{
            LogManager.getInstance().logCharEvent("MovimentoParaCima", new Date(),_main.editFlag);
        }

    }

    public void onSwipeBottom() {
        if(_main.editFlag) {

            //obter opções de clipboard
            if (_main.selecaoAtiva && !_main.moveSelection) {
                if (_main.actualPositionOfCursor != _main.posicaoInicioSelecao) {
                    numeroClipboardOption--;
                    if(numeroClipboardOption==5){
                        clipboardOperation = "selecionarTudo";
                        _main.t1.speak("Selecionar Tudo", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else if (numeroClipboardOption == 4) {
                        clipboardOperation = "ouvirSelecao";
                        _main.t1.speak("Ouvir a seleção", TextToSpeech.QUEUE_FLUSH, null);
                    } else if (numeroClipboardOption == 3) {
                        if (!textoCopiado.isEmpty()) {
                            clipboardOperation = "colar";
                            _main.t1.speak("Colar", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        else{
                            numeroClipboardOption = 2;
                            clipboardOperation = "cortar";
                            _main.t1.speak("Cortar", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else if (numeroClipboardOption == 2) {
                        clipboardOperation = "cortar";
                        _main.t1.speak("Cortar", TextToSpeech.QUEUE_FLUSH, null);
                    } else if (numeroClipboardOption == 1) {
                        clipboardOperation = "copiar";
                        _main.t1.speak("Copiar", TextToSpeech.QUEUE_FLUSH, null);
                    } else if (numeroClipboardOption == 0) {
                        clipboardOperation = "cancelar";
                        _main.t1.speak("Cancelar", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        numeroClipboardOption = 5;
                        clipboardOperation = "selecionarTudo";
                        _main.t1.speak("Selecionar Tudo", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                else{
                    if (!textoCopiado.isEmpty()) {
                        if (clipboardOperation.equals("cancelar")) {
                            clipboardOperation = "selecionarTudo";
                            _main.t1.speak("Selecionar Tudo", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        else if(clipboardOperation.equals("selecionarTudo")) {
                            clipboardOperation = "colar";
                            _main.t1.speak("Colar", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        else {
                            clipboardOperation = "cancelar";
                            _main.t1.speak("Cancelar", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                    else{
                        if (!clipboardOperation.equals("cancelar")) {
                            clipboardOperation = "cancelar";
                            _main.t1.speak("Cancelar", TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            clipboardOperation = "selecionarTudo";
                            _main.t1.speak("Selecionar Tudo", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }

                }
            }//mover a selecao
            else if(_main.selecaoAtiva && _main.moveSelection){
                Log.v(_main.DEBUG_TAG, "Seleção movimentada para a baixo");
            }
            else{
                //dont do anything
                Log.v(_main.DEBUG_TAG, "Nada para fazer no OnSwipeBottom");
            }
            LogManager.getInstance().logCharEvent("MovimentoParaBaixo", new Date(),_main.editFlag);
        }

        else{
            LogManager.getInstance().logCharEvent("MovimentoParaBaixo", new Date(),_main.editFlag);
        }

    }

}