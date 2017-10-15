package com.example.danieltrindade.brailler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;
import android.speech.tts.TextToSpeech;

import java.util.Locale;
import java.util.Date;

public class MainActivity extends Activity {

    //TAG
    protected String TAG = "Brailler++";
    //Gesture Testing
    protected String DEBUG_TAG = "Gestures";

    // connection to the Arduino
    private static final String DEVICE_ADDRESS = "00:06:66:6C:A7:44"; //endereço do bluetooth
    private BluetoothReceiver mBTReceiver = new BluetoothReceiver(); //iniciar o bluetooth

    //Views
    public View mInputView;

    //Text to Speech
    TextToSpeech t1;

    //Sensor
    boolean orientMe=false; //usado para o utilizador ouvir a posição atual do telemóvel. Não usado para testes
    String mobilePosition; //usado se quiser saber a posição atual do telemóvel. Não usado para testes
    boolean rotationBlocked=false; //usado se quiser bloquear a posição atual do telemóvel. Não usado para testes

    //Translations
    Translation translation = new Translation(this);

    //preferencias
    Preferencias preferencias = new Preferencias(this);
    boolean preferenciasGlobal=true; //usado para determinar se o utilizador esta na área de preferências
    boolean preferencias1 = true; //usado para determinar o alfabeto (esquerda ou direita)
    boolean preferencias2 = false; //usado para determinar o carater espaço
    boolean preferencias3 = false; //usado para determinar o carater de apagar
    boolean inicioApp = false; //usado para o tts não interferir com a descrição inicial das preferencias. Não usado para testes
    String posicaoPredefinidaDoTelemovel=""; //posição predefenida do telemóvel
    String posicaoEspaco=""; //posição do espaço
    String posicaoApagar=""; //posição para apagar
    String posicaoEnter=""; //posição para Enter

    //variaveis globais
    String word = "";
    String text = "";
    String line = "";
    boolean numberFlag = false;
    boolean lettersLowerCase = true;
    boolean lettersUpperCase = false;
    public boolean editFlag = false;
    public boolean edicaoCarateres = false;
    public boolean edicaoPalavras = false;

    //variaveis para editar
    EditText tv;
    int actualPositionOfCursor = 0;
    String lastLine = "";
    boolean selecaoAtiva = false;
    String selectedSentence = "";
    int posicaoInicioSelecao = -1;
    public boolean moveSelection=false;

    //cronometros/temporizadores
    Stopwatch timer = new Stopwatch(); //ainda não é usado mas pode vir a ser.
    CountDownTimer countDownTimer; //usado para determinar quando o tts da rotação pode ser ativo. Não usado para testes

    //usado para determinar se já houve um início da app
    static boolean resume = false;

    //usado para inicializar o XML dos testes
    boolean initializeXML=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*Para testes*/
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        rotationBlocked = true;

        //esconde painel de definicoes rapidas
        WindowManager manager = ((WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = (int) (50 * getResources()
                .getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;
        CustomViewGroup view = new CustomViewGroup(this);
        manager.addView(view, localLayoutParams);

        //register broadcast receivers
        registerReceiver(mBTReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
        registerReceiver(mBTReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECTED));

        //connects to BT module
        Amarino.connect(getApplicationContext(), DEVICE_ADDRESS);

        //initialize text to speech
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    //t1.setLanguage(Locale.ENGLISH);
                    t1.setLanguage(new Locale("pt"));
                }
            }
        });

        /*NÃO USADO NOS TESTES*/
        countDownTimer = new CountDownTimer(9428,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                inicioApp = false;
            }
        };

        mInputView = (View) getLayoutInflater().inflate(R.layout.activity_main, null);

        setContentView(mInputView);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll_mask);

        linearLayout.setOnTouchListener(new OnSwipeTouchListener(this,this){

        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        orientMe = true;
        mobilePosition = getRotation(this,orientMe);

        super.onConfigurationChanged(newConfig);

    }


    private class BluetoothReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            //conecta com o Bluetooth do Arduino
            if(action.equalsIgnoreCase(AmarinoIntent.ACTION_CONNECTED))
            {
                Log.v(TAG, "connected");
                t1.speak("Conectado", TextToSpeech.QUEUE_FLUSH, null);

                /*NÃO USADO NOS TESTES*/
                countDownTimer.start();
                inicioApp = true;

                if (resume==false){
                    t1.speak("Bem vindo ao sistema Brailler++. Por favor " +
                            "escreva a letra, A.", TextToSpeech.QUEUE_ADD,null);
                    /*NÃO USADO NOS TESTES*/
                    //t1.speak("Bem vindo ao sistema Brailler++. Por favor posicione o telemóvel como bem desejar, e " +
                    //    "escreva a letra, A.", TextToSpeech.QUEUE_ADD,null);
                    resume = true;
                }

                /*NÃO USADO NOS TESTES*/
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                //rotationBlocked = false;

            }

            // Maybe in the future
            /*else if(action.equalsIgnoreCase(AmarinoIntent.ACTION_CONNECTION_FAILED)){
                Amarino.connect(getApplicationContext(), DEVICE_ADDRESS);
            }*/

            //receive data
            else if(action.equalsIgnoreCase(AmarinoIntent.ACTION_RECEIVED))
            {
                final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);

                if(dataType == AmarinoIntent.STRING_EXTRA){
                    String data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);

                    Log.v(TAG,"AQUI ESTA => " + data);

                    if(data != null && !preferenciasGlobal){
                        if (initializeXML){
                            LogManager.getInstance().startSession();
                            initializeXML=false;
                        }

                        processWriting(data);
                        // TODO whatever you want
                    }

                    if(data!= null && preferencias1) {
                        if (preferencias.selecao_preferencias_posicao_telemovel(data).equals("Success")) {
                            data = null;
                            preferencias1 = false;
                            preferencias2 = true;
                            t1.speak("Escolha o botão da coluna do meio, que deseja usar como espaço", TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            t1.speak("A posição escolhida não é característica de um, A.", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                    if(data!=null && preferencias2) {
                        if(preferencias.selecao_preferencias_botoes(data,posicaoPredefinidaDoTelemovel,"espaço").equals("Success")){
                            data = null;
                            preferencias2 = false;
                            preferencias3 = true;
                            t1.speak("Selecione outro botão da coluna do meio, que deseja usar para apagar", TextToSpeech.QUEUE_FLUSH,null);
                        }
                        else{
                            t1.speak("Posição inválida. Por favor," +
                                    "escolha um botão da coluna do meio, que deseja usar como espaço", TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                    if (data!=null && preferencias3){
                        if(preferencias.selecao_preferencias_botoes(data,posicaoPredefinidaDoTelemovel,"apagar").equals("Success")){
                            data = null;
                            preferencias3 = false;
                            preferenciasGlobal = false;
                            initializeXML=true;
                            t1.speak("Obrigado. A posição da coluna do meio que não escolheu, irá servir para inserir uma nova linha! " +
                                    "Está pronto para escrever!", TextToSpeech.QUEUE_FLUSH,null);
                        }
                        else{
                            t1.speak("Posição inválida. Por favor," +
                                    "escolha um botão da coluna do meio, que deseja usar para apagar", TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }

                }
            }

        }
    }

    public class CustomViewGroup extends ViewGroup {
        public CustomViewGroup(Context context) {
            super(context);
        }
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Log.v("CustomViewGroup", "**********Intercepted");
            return true;
        }
    }

    public String getRotation(Context context, boolean orientMe){
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (rotation) {
            case Surface.ROTATION_0:
                if (orientMe && !inicioApp) {
                    t1.speak("Modo de Retrato", TextToSpeech.QUEUE_ADD, null);
                    orientMe = false;
                }
                Log.v(TAG, "Posição vertical!");
                return "portrait";
            case Surface.ROTATION_90:
                if (orientMe && !inicioApp) {
                    t1.speak("Modo de Paisagem", TextToSpeech.QUEUE_ADD, null);
                    orientMe = false;
                }
                Log.v(TAG, "Posição horizontal para a esquerda!");
                return "reverse landscape";
            case Surface.ROTATION_180:
                if (orientMe && !inicioApp) {
                    t1.speak("Modo de Retrato", TextToSpeech.QUEUE_ADD, null);
                    orientMe = false;
                }
                Log.v(TAG, "Posição vertical!");
                return "reverse portrait";
            case Surface.ROTATION_270:
                if(orientMe && !inicioApp){
                    t1.speak("Modo de Paisagem",TextToSpeech.QUEUE_ADD,null);
                    orientMe = false;
                }
                Log.v(TAG, "Posição horizontal para a direita!");
                return "landscape";
        }
        return "";
    }

    @SuppressLint("DefaultLocale")
    public void processWriting(String data) {

        orientMe = false;


        //TextView tv = (TextView) findViewById(R.id.textView);
        tv = (EditText) findViewById(R.id.editText);

        tv.setSingleLine(false);

        if (data.contains("serial ok")) {
            LogManager.getInstance().logCharEvent("TecladoReconfigurado", new Date(),editFlag);
            t1.speak("Teclado reconfigurado", TextToSpeech.QUEUE_FLUSH, null);
        }

        else{

            String dataTransmitida = translation.translateData(data);

            Log.v(TAG, "caraterProduzido: " + dataTransmitida);

            if (dataTransmitida.equals("modo de edição carateres")) {
                //editFlag=!editFlag;
                //lettersLowerCase=!lettersLowerCase;

                if (edicaoCarateres) {
                    edicaoCarateres = false;
                    editFlag = false;
                    edicaoPalavras = false;
                    LogManager.getInstance().logCharEvent("EdiçãoDesativa", new Date(),editFlag);
                    t1.speak("Modo de edição desativo", TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    boolean soMovimentoAlterado = false;
                    if (edicaoPalavras) {
                        soMovimentoAlterado = true;
                    }
                    editFlag = true;
                    edicaoCarateres = true;
                    edicaoPalavras = false;
                    if (soMovimentoAlterado) {
                        LogManager.getInstance().logCharEvent("MovimentoPorCarateres", new Date(),editFlag);
                        t1.speak("Movimento por carateres", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        LogManager.getInstance().logCharEvent("EdiçãoAtiva(carateres)", new Date(),editFlag);
                        t1.speak("Modo de edição ativo", TextToSpeech.QUEUE_FLUSH, null);
                        t1.speak("Movimento por carateres", TextToSpeech.QUEUE_ADD, null);
                    }

                }


            } else if (dataTransmitida.equals("modo de edição palavras")) {
                if (edicaoPalavras) {
                    edicaoCarateres = false;
                    editFlag = false;
                    edicaoPalavras = false;
                    LogManager.getInstance().logCharEvent("EdiçãoDesativa", new Date(),editFlag);
                    t1.speak("Modo de edição desativo", TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    boolean soMovimentoAlterado = false;
                    if (edicaoCarateres) {
                        soMovimentoAlterado = true;
                    }
                    editFlag = true;
                    edicaoPalavras = true;
                    edicaoCarateres = false;
                    if (soMovimentoAlterado) {
                        LogManager.getInstance().logCharEvent("MovimentoPorPalavras", new Date(),editFlag);
                        t1.speak("Movimento por palavras", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        LogManager.getInstance().logCharEvent("EdiçãoAtiva(palavras)", new Date(),editFlag);
                        t1.speak("Modo de edição ativo", TextToSpeech.QUEUE_FLUSH, null);
                        t1.speak("Movimento por palavras", TextToSpeech.QUEUE_ADD, null);
                    }
                }

            } else {

                //if (!editFlag){

                //to end the logging
                if (dataTransmitida.equals("fim da frase")) {

                    t1.speak("Nova frase", TextToSpeech.QUEUE_FLUSH, null);
                    t1.speak(text, TextToSpeech.QUEUE_ADD, null);

                    Log.v(TAG, "going to end the XML logging.");
                    // save file
                    String fileName = LogManager.getInstance().endSession(text);
                    // send filename to editor
                    Bundle bundle = new Bundle();
                    bundle.putString("filename", fileName);
                    Intent command = new Intent("imelistener");
                    command.putExtra("filename", fileName);
                    getApplicationContext().sendBroadcast(command);

                    word = "";
                    line = "";
                    text = "";

                    tv.setText("");
                    actualPositionOfCursor = tv.getText().length();

                    editFlag=false;
                    edicaoCarateres=false;
                    edicaoPalavras=false;
                    selectedSentence="";
                    selecaoAtiva=false;
                    posicaoInicioSelecao=-1;

                    OnSwipeTouchListener onSwippeTouchListener = new OnSwipeTouchListener(this, this);
                    onSwippeTouchListener.init();

                    Log.v(TAG, "word: " + word);
                    Log.v(TAG, "line: " + line);
                    Log.v(TAG, "text: " + text);
                    initializeXML = true;
                } else if (dataTransmitida.equals("Espaço")) {

                    if (selectedSentence != "") {
                        if (actualPositionOfCursor == posicaoInicioSelecao) {
                            text = text.substring(0, actualPositionOfCursor) + " " +
                                    text.substring(actualPositionOfCursor, text.length());
                        }
                        if (actualPositionOfCursor < posicaoInicioSelecao) {
                            text = text.substring(0, actualPositionOfCursor) + " " +
                                    text.substring(posicaoInicioSelecao, text.length());
                        }
                        if (actualPositionOfCursor > posicaoInicioSelecao) {
                            text = text.substring(0, posicaoInicioSelecao) + " " +
                                    text.substring(actualPositionOfCursor, text.length());
                            actualPositionOfCursor = posicaoInicioSelecao;
                        }

                        String[] linhas = text.split("↵");
                        int incremetador = 0;
                        int positionOfWord = 0;

                        for (int i = 0; i < linhas.length; i++) {
                            if (linhas[i].length() + incremetador >= actualPositionOfCursor) {
                                line = linhas[i];
                                positionOfWord = actualPositionOfCursor - incremetador;
                                break;
                            }
                            incremetador = incremetador + linhas[i].length() + 1;
                        }

                        String[] palavras = line.split(" ");
                        incremetador = 0;
                        boolean detectedWord = false;

                        for (int i = 0; i < palavras.length; i++) {
                            if (palavras[i].length() + incremetador >= positionOfWord) {
                                word = palavras[i].substring(0, positionOfWord - incremetador);
                                detectedWord = true;
                                Log.v(TAG, "última palavra = " + word);
                                break;
                            }
                            incremetador = incremetador + palavras[i].length() + 1;
                        }

                        if (!detectedWord) {
                            Log.v(TAG, "Palavra apagada por causa do espaço" +
                                    "");
                            word = "";
                        }

                        t1.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                        int start = Math.max(tv.getSelectionStart(), 0);
                        int end = Math.max(tv.getSelectionEnd(), 0);
                        tv.getText().replace(Math.min(start, end), Math.max(start, end),
                                " ", 0, 1);

                        word = "";

                        actualPositionOfCursor = actualPositionOfCursor + 1;
                        tv.setSelection(actualPositionOfCursor);
                        selectedSentence = "";
                        moveSelection = false;
                        posicaoInicioSelecao = -1;

                        Log.v(TAG, "word: " + word);
                        Log.v(TAG, "line: " + line);
                        Log.v(TAG, "text: " + text);
                        LogManager.getInstance().logCharEvent(" ", new Date(),editFlag);

                    } else {
                        text = text.substring(0, actualPositionOfCursor) + " " +
                                text.substring(actualPositionOfCursor, text.length());

                        String[] linhas = text.split("↵");
                        int incremetador = 0;
                        int positionOfWord = 0;

                        for (int i = 0; i < linhas.length; i++) {
                            if (linhas[i].length() + incremetador >= actualPositionOfCursor) {
                                line = linhas[i];
                                positionOfWord = actualPositionOfCursor - incremetador;
                                break;
                            }
                            incremetador = incremetador + linhas[i].length() + 1;
                        }

                        String[] palavras = line.split(" ");
                        incremetador = 0;
                        for (int i = 0; i < palavras.length; i++) {
                            if (palavras[i].length() + incremetador >= positionOfWord) {
                                word = palavras[i].substring(0, positionOfWord - incremetador);
                                Log.v(TAG, "última palavra = " + word);
                                break;
                            }
                            incremetador = incremetador + palavras[i].length() + 1;
                        }

                        t1.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                        int start = Math.max(tv.getSelectionStart(), 0);
                        int end = Math.max(tv.getSelectionEnd(), 0);
                        tv.getText().replace(Math.min(start, end), Math.max(start, end),
                                " ", 0, 1);

                        word = "";

                        actualPositionOfCursor = actualPositionOfCursor + 1;

                        Log.v(TAG, "word: " + word);
                        Log.v(TAG, "line: " + line);
                        Log.v(TAG, "text: " + text);
                        LogManager.getInstance().logCharEvent(" ", new Date(),editFlag);
                    }
                } else if (dataTransmitida.equals("Apagar")) {

                    if (!text.isEmpty()) {

                        if (selectedSentence != "") {

                            String deletedSentence = "";

                            if (actualPositionOfCursor == posicaoInicioSelecao) {
                                deletedSentence = Character.toString(text.charAt(actualPositionOfCursor - 1));
                                text = text.substring(0, actualPositionOfCursor - 1) +
                                        text.substring(actualPositionOfCursor, text.length());
                            }
                            if (actualPositionOfCursor < posicaoInicioSelecao) {
                                deletedSentence = text.substring(actualPositionOfCursor, posicaoInicioSelecao);
                                text = text.substring(0, actualPositionOfCursor) +
                                        text.substring(posicaoInicioSelecao, text.length());
                            }
                            if (actualPositionOfCursor > posicaoInicioSelecao) {
                                deletedSentence = text.substring(posicaoInicioSelecao, actualPositionOfCursor);
                                text = text.substring(0, posicaoInicioSelecao) +
                                        text.substring(actualPositionOfCursor, text.length());
                                actualPositionOfCursor = posicaoInicioSelecao;
                            }

                            line = "";
                            lastLine = "";

                            int incremetador = 0;
                            int positionOfWord = 0;

                            tv.setText("");
                            String textoAColocar = text.replace('↵', '\n');
                            tv.append(textoAColocar);
                            String[] linhasDisponiveis = textoAColocar.split("\n");

                            for (int i = 0; i < linhasDisponiveis.length; i++) {
                                if (linhasDisponiveis[i].length() + incremetador + 1 >= actualPositionOfCursor) {
                                    line = linhasDisponiveis[i];
                                    positionOfWord = actualPositionOfCursor - incremetador;
                                    break;
                                }
                                incremetador = incremetador + linhasDisponiveis[i].length() + 1;
                            }

                            String[] palavras = line.split(" ");
                            incremetador = 0;
                            word = "";

                            for (int i = 0; i < palavras.length; i++) {
                                if (palavras[i].length() + incremetador + 1 > positionOfWord) { //vinha com igual
                                    if (positionOfWord - incremetador - 1 == -1) {
                                        word = "";
                                        break;
                                    } else {
                                        word = palavras[i].substring(0, positionOfWord - incremetador);
                                        break;
                                    }
                                }
                                incremetador = incremetador + palavras[i].length() + 1;
                            }

                            //actualPositionOfCursor = actualPositionOfCursor - 1;
                            tv.setSelection(actualPositionOfCursor);

                            selectedSentence = "";
                            moveSelection = false;
                            posicaoInicioSelecao = -1;

                            Log.v(TAG, "word: " + word);
                            Log.v(TAG, "line: " + line);
                            Log.v(TAG, "text: " + text);

                            Log.v(TAG, "deleted sentence : " + deletedSentence);

                            String avaliarConteudoApagado = deletedSentence;
                            String[] conteudoSemEspacos = avaliarConteudoApagado.split(" ");
                            boolean itContainsOnlySpaces = false;
                            boolean itContainsNewLines = false;
                            boolean itContainsChar = false;

                            if (conteudoSemEspacos.length == 0) {
                                itContainsOnlySpaces = true;
                            }
                            if (conteudoSemEspacos.length > 0) {
                                for (int i = 0; i < conteudoSemEspacos.length; i++) {
                                    if (!conteudoSemEspacos[i].isEmpty()) {
                                        String[] resultadoTemporario = conteudoSemEspacos[i].split("↵");
                                        if (resultadoTemporario.length != 0) {
                                            itContainsChar = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!itContainsChar && !itContainsOnlySpaces) {
                                itContainsNewLines = true;
                            }

                            if (itContainsNewLines) {
                                t1.speak("Linha apagada", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (itContainsOnlySpaces) {
                                t1.speak("Espaço apagado", TextToSpeech.QUEUE_FLUSH, null);
                            } else {
                                t1.speak("Apagado", TextToSpeech.QUEUE_FLUSH, null);
                                t1.speak(deletedSentence, TextToSpeech.QUEUE_ADD, null);
                            }

                        } else {

                            if (actualPositionOfCursor == 0) {
                                t1.speak("O cursor está no início do texto", TextToSpeech.QUEUE_FLUSH, null);
                            } else {
                                String deletedChar = Character.toString(text.charAt(actualPositionOfCursor - 1));

                                text = text.substring(0, actualPositionOfCursor - 1) +
                                        text.substring(actualPositionOfCursor, text.length());

                                line = "";
                                lastLine = "";

                                int incremetador = 0;
                                int positionOfWord = 0;

                                tv.setText("");
                                String textoAColocar = text.replace('↵', '\n');
                                tv.append(textoAColocar);
                                String[] linhasDisponiveis = textoAColocar.split("\n");

                                for (int i = 0; i < linhasDisponiveis.length; i++) {
                                    if (linhasDisponiveis[i].length() + incremetador + 1 >= actualPositionOfCursor) {
                                        line = linhasDisponiveis[i];
                                        positionOfWord = actualPositionOfCursor - incremetador;
                                        break;
                                    }
                                    incremetador = incremetador + linhasDisponiveis[i].length() + 1;
                                }

                                String[] palavras = line.split(" ");
                                incremetador = 0;

                                for (int i = 0; i < palavras.length; i++) {
                                    if (palavras[i].length() + incremetador + 1 >= positionOfWord) {
                                        if (positionOfWord - incremetador - 1 == -1) {
                                            word = "";
                                            break;
                                        } else {
                                            word = palavras[i].substring(0, positionOfWord - incremetador - 1);
                                            break;
                                        }
                                    }
                                    incremetador = incremetador + palavras[i].length() + 1;
                                }

                                actualPositionOfCursor = actualPositionOfCursor - 1;
                                tv.setSelection(actualPositionOfCursor);


                                Log.v(TAG, "word: " + word);
                                Log.v(TAG, "line: " + line);
                                Log.v(TAG, "text: " + text);

                                Log.v(TAG, "deleted char: " + deletedChar);

                                if (deletedChar.equals("↵")) {
                                    t1.speak("Linha apagada", TextToSpeech.QUEUE_FLUSH, null);
                                } else if (deletedChar.equals(" ")) {
                                    t1.speak("Espaço apagado", TextToSpeech.QUEUE_FLUSH, null);
                                } else {
                                    t1.speak("Apagado", TextToSpeech.QUEUE_FLUSH, null);
                                    t1.speak(deletedChar, TextToSpeech.QUEUE_ADD, null);
                                }

                            }
                        }
                        LogManager.getInstance().logCharEvent("/", new Date(),editFlag);
                    } else {
                        t1.speak("Não existe nada para apagar", TextToSpeech.QUEUE_FLUSH, null);
                        LogManager.getInstance().logCharEvent("/", new Date(),editFlag);
                    }
                } else if (dataTransmitida.equals("Enter")) {

                    if (selectedSentence != "") {
                        if (actualPositionOfCursor == posicaoInicioSelecao) {
                            text = text.substring(0, actualPositionOfCursor) + "↵" +
                                    text.substring(actualPositionOfCursor, text.length());
                        }
                        if (actualPositionOfCursor < posicaoInicioSelecao) {
                            text = text.substring(0, actualPositionOfCursor) + "↵" +
                                    text.substring(posicaoInicioSelecao, text.length());
                            Log.v(TAG, "text : " + text);

                        }
                        if (actualPositionOfCursor > posicaoInicioSelecao) {
                            text = text.substring(0, posicaoInicioSelecao) + "↵" +
                                    text.substring(actualPositionOfCursor, text.length());
                            Log.v(TAG, "text : " + text);
                            actualPositionOfCursor = posicaoInicioSelecao;
                        }

                        word = "";
                        String linhasAnteriorAReter = text.substring(0, actualPositionOfCursor);
                        String linhaAReter = text.substring(actualPositionOfCursor + 1, text.length());
                        String linhaAnterior = "";

                        if (!linhasAnteriorAReter.isEmpty()) {
                            if (linhasAnteriorAReter.substring(linhasAnteriorAReter.length() - 1).equals("↵")) {
                                linhaAnterior = "";
                            }
                            if (!linhasAnteriorAReter.substring(linhasAnteriorAReter.length() - 1).equals("↵")) {
                                String[] linhasAnteriores = linhasAnteriorAReter.split("↵");
                                linhaAnterior = linhasAnteriores[linhasAnteriores.length - 1];
                            }
                        }
                        if (linhasAnteriorAReter.isEmpty()) {
                            linhaAnterior = "";
                        }


                        Log.v(TAG, "Ultima linha : " + linhaAnterior);

                        t1.speak("Nova linha", TextToSpeech.QUEUE_FLUSH, null);
                        t1.speak(linhaAnterior, TextToSpeech.QUEUE_ADD, null);
                        lastLine = linhaAnterior;


                        String[] linhas = linhaAReter.split("↵");
                        line = linhas[0];

                        int start = Math.max(tv.getSelectionStart(), 0);
                        int end = Math.max(tv.getSelectionEnd(), 0);

                        tv.getText().replace(Math.min(start, end), Math.max(start, end),
                                "\n", 0, 1);
                        actualPositionOfCursor = actualPositionOfCursor + 1;
                        tv.setSelection(actualPositionOfCursor);

                        selectedSentence = "";
                        moveSelection = false;
                        posicaoInicioSelecao = -1;
                        Log.v(TAG, "word: " + word);
                        Log.v(TAG, "line: " + line);
                        Log.v(TAG, "text: " + text);
                        LogManager.getInstance().logCharEvent("↵", new Date(),editFlag);

                    } else {
                        word = "";
                        String linhasAnteriorAReter = text.substring(0, actualPositionOfCursor);
                        String linhaAReter = text.substring(actualPositionOfCursor, text.length());
                        text = text.substring(0, actualPositionOfCursor) + "↵" +
                                text.substring(actualPositionOfCursor, text.length());
                        String linhaAnterior = "";

                        if (!linhasAnteriorAReter.isEmpty()) {
                            if (linhasAnteriorAReter.substring(linhasAnteriorAReter.length() - 1).equals("↵")) {
                                linhaAnterior = "";
                            }
                            if (!linhasAnteriorAReter.substring(linhasAnteriorAReter.length() - 1).equals("↵")) {
                                String[] linhasAnteriores = linhasAnteriorAReter.split("↵");
                                linhaAnterior = linhasAnteriores[linhasAnteriores.length - 1];
                            }
                        }
                        if (linhasAnteriorAReter.isEmpty()) {
                            linhaAnterior = "";
                        }


                        Log.v(TAG, "Ultima linha : " + linhaAnterior);

                        t1.speak("Nova linha", TextToSpeech.QUEUE_FLUSH, null);
                        t1.speak(linhaAnterior, TextToSpeech.QUEUE_ADD, null);
                        lastLine = linhaAnterior;


                        String[] linhas = linhaAReter.split("↵");
                        if (linhas.length == 0) {
                            line = "";
                        } else {
                            line = linhas[0];
                        }

                        int start = Math.max(tv.getSelectionStart(), 0);
                        int end = Math.max(tv.getSelectionEnd(), 0);

                        tv.getText().replace(Math.min(start, end), Math.max(start, end),
                                "\n", 0, 1);
                        actualPositionOfCursor = actualPositionOfCursor + 1;

                        Log.v(TAG, "word: " + word);
                        Log.v(TAG, "line: " + line);
                        Log.v(TAG, "text: " + text);
                        LogManager.getInstance().logCharEvent("↵", new Date(),editFlag);

                    }

                            /*codigo correto para bloquear ecrã com um acorde*/
                            /*mobilePosition = getRotation(this,false);
                            //boolean rotationBlocked;
                            if (mobilePosition.equals("portrait") && rotationBlocked==false){
                                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                t1.speak("Rotação de ecrã bloqueada",TextToSpeech.QUEUE_FLUSH,null);
                                rotationBlocked = true;
                            }
                            else if(mobilePosition.equals("reverse landscape") && rotationBlocked==false){
                                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                t1.speak("Rotação de ecrã bloqueada",TextToSpeech.QUEUE_FLUSH,null);
                                rotationBlocked = true;
                            }
                            else if(mobilePosition.equals("landscape") && rotationBlocked==false){
                                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                                t1.speak("Rotação de ecrã bloqueada",TextToSpeech.QUEUE_FLUSH,null);
                                rotationBlocked = true;
                            }
                            else if(mobilePosition.equals("reverse portrait") && rotationBlocked==false){
                                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                                t1.speak("Rotação de ecrã bloqueada",TextToSpeech.QUEUE_FLUSH,null);
                                rotationBlocked = true;
                            }
                            else{
                                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                t1.speak("Rotação de ecrã desbloqueada",TextToSpeech.QUEUE_FLUSH,null);
                                rotationBlocked = false;
                            }*/

                } else {
                    if (dataTransmitida.isEmpty()) {
                        t1.speak("Combinação Inválida", TextToSpeech.QUEUE_FLUSH, null);
                        LogManager.getInstance().logCharEvent("X", new Date(),editFlag);
                    } else if (dataTransmitida.equals("activateNumbers123")) {
                        if (numberFlag) {
                            numberFlag = false;
                            lettersUpperCase = false;
                            lettersLowerCase = true;
                            t1.speak("Modo Numérico desativado", TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            numberFlag = true;
                            lettersUpperCase = false;
                            lettersLowerCase = false;
                            t1.speak("Modo Numérico ativado", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else if (dataTransmitida.equals("activateUpperCase123")) {
                        if (lettersUpperCase) {
                            lettersUpperCase = false;
                            lettersLowerCase = true;
                            numberFlag = false;
                            t1.speak("Letras Maiúsculas desativas", TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            lettersUpperCase = true;
                            lettersLowerCase = false;
                            numberFlag = false;
                            t1.speak("Letras Maiúsculas ativas", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else if (dataTransmitida.equals("activateLowerCase123")) {
                        numberFlag = false;
                        lettersLowerCase = true;
                        lettersUpperCase = false;
                        t1.speak("Letras Minúsculas ativas", TextToSpeech.QUEUE_FLUSH, null);
                    } else {

                        if (selectedSentence != "") {
                            if (actualPositionOfCursor == posicaoInicioSelecao) {
                                text = text.substring(0, actualPositionOfCursor) + dataTransmitida +
                                        text.substring(actualPositionOfCursor, text.length());
                            }
                            if (actualPositionOfCursor < posicaoInicioSelecao) {
                                text = text.substring(0, actualPositionOfCursor) + dataTransmitida +
                                        text.substring(posicaoInicioSelecao, text.length());
                            }
                            if (actualPositionOfCursor > posicaoInicioSelecao) {
                                text = text.substring(0, posicaoInicioSelecao) + dataTransmitida +
                                        text.substring(actualPositionOfCursor, text.length());
                                actualPositionOfCursor = posicaoInicioSelecao;
                            }

                            String[] linhas = text.split("↵");
                            int incremetador = 0;
                            int positionOfWord = 0;

                            for (int i = 0; i < linhas.length; i++) {
                                if (linhas[i].length() + incremetador >= actualPositionOfCursor) {
                                    line = linhas[i];
                                    positionOfWord = actualPositionOfCursor - incremetador;
                                    break;
                                }
                                incremetador = incremetador + linhas[i].length() + 1;
                            }

                            String[] palavras = line.split(" ");
                            incremetador = 0;
                            for (int i = 0; i < palavras.length; i++) {
                                if (palavras[i].length() + incremetador >= positionOfWord) {
                                    word = palavras[i].substring(0, positionOfWord - incremetador + 1);
                                    break;
                                }
                                incremetador = incremetador + palavras[i].length() + 1;
                            }

                            int start = Math.max(tv.getSelectionStart(), 0);
                            int end = Math.max(tv.getSelectionEnd(), 0);
                            tv.getText().replace(Math.min(start, end), Math.max(start, end),
                                    dataTransmitida, 0, dataTransmitida.length());
                            actualPositionOfCursor = actualPositionOfCursor + 1;
                            tv.setSelection(actualPositionOfCursor);
                            t1.speak(dataTransmitida, TextToSpeech.QUEUE_FLUSH, null);

                            selectedSentence = "";
                            moveSelection = false;
                            posicaoInicioSelecao = -1;

                            Log.v(TAG, "word: " + word);
                            Log.v(TAG, "line: " + line);
                            Log.v(TAG, "text: " + text);

                            LogManager.getInstance().logCharEvent(dataTransmitida, new Date(),editFlag);
                        } else {
                            text = text.substring(0, actualPositionOfCursor) + dataTransmitida +
                                    text.substring(actualPositionOfCursor, text.length());

                            String[] linhas = text.split("↵");
                            int incremetador = 0;
                            int positionOfWord = 0;

                            for (int i = 0; i < linhas.length; i++) {
                                if (linhas[i].length() + incremetador >= actualPositionOfCursor) {
                                    line = linhas[i];
                                    positionOfWord = actualPositionOfCursor - incremetador;
                                    break;
                                }
                                incremetador = incremetador + linhas[i].length() + 1;
                            }

                            String[] palavras = line.split(" ");
                            incremetador = 0;
                            for (int i = 0; i < palavras.length; i++) {
                                if (palavras[i].length() + incremetador >= positionOfWord) {
                                    word = palavras[i].substring(0, positionOfWord - incremetador + 1);
                                    break;
                                }
                                incremetador = incremetador + palavras[i].length() + 1;
                            }

                            Log.v(TAG, "word: " + word);
                            Log.v(TAG, "line: " + line);
                            Log.v(TAG, "text: " + text);
                            int start = Math.max(tv.getSelectionStart(), 0);
                            int end = Math.max(tv.getSelectionEnd(), 0);
                            tv.getText().replace(Math.min(start, end), Math.max(start, end),
                                    dataTransmitida, 0, dataTransmitida.length());
                            actualPositionOfCursor = actualPositionOfCursor + 1;
                            t1.speak(dataTransmitida, TextToSpeech.QUEUE_FLUSH, null);
                            LogManager.getInstance().logCharEvent(dataTransmitida, new Date(),editFlag);
                        }

                    }
                }
                //}

                    /*else if(editFlag){
                        if(dataTransmitida.equals("iniciotexto")){
                            tv.setSelection(0);
                            actualPositionOfCursor = 0;
                            selectedSentence="";
                            t1.speak("Início do texto", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        else if(dataTransmitida.equals("fimtexto")){
                            tv.setSelection(text.length());
                            actualPositionOfCursor = text.length();
                            selectedSentence="";
                            t1.speak("Fim do texto", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        else if(dataTransmitida.equals("selecionartudo")){
                            tv.setSelection(0,text.length());
                            posicaoInicioSelecao=0;
                            actualPositionOfCursor = text.length();
                            selectedSentence = text;
                            selecaoAtiva=false;
                            t1.speak("Texto selecionado", TextToSpeech.QUEUE_FLUSH, null);
                            t1.speak(text, TextToSpeech.QUEUE_ADD, null);
                        }
                    }*/
                //else{
                //do nothing
                //}

            }
        }
    }

    @Override
    public void onPause(){
        /*if(t1!=null){
            t1.stop();
            t1.shutdown();
        }*/
        if (resume){
            Amarino.disconnect(this, DEVICE_ADDRESS);
            unregisterReceiver(mBTReceiver);
        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        if (resume){
            registerReceiver(mBTReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
            registerReceiver(mBTReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECTED));
            Amarino.connect(this,DEVICE_ADDRESS);
        }
        super.onResume();
    }

    @Override
    public void onDestroy(){

        // disconnects to BT module
        Amarino.disconnect(this, DEVICE_ADDRESS);

        // un-registers broadcast receiver
        unregisterReceiver(mBTReceiver);

        //shutdown tts
        if(t1!=null){
            t1.stop();
            t1.shutdown();
        }

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.removeView(mInputView);

        super.onDestroy();

    }

}