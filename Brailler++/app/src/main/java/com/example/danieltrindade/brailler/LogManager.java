package com.example.danieltrindade.brailler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.graphics.Point;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

/**
 * Created by Daniel Trindade on 23/04/2017.
 */

public class LogManager {

    private static LogManager mInstance = null;

    private final String SESSION = "session";
    private final String DOWN = "regDown";
    private final String UP = "regUp";
    private final String MOVE = "regMove";
    private final String CHAR = "regChar";
    private final String SWIPE = "regSwipe";
    private final String CENTROID_UPDATE = "regCentroidUpdate";
    private final String CENTROID_CALIBRATION = "regCentroidCalibration";
    private final String INPUT_STREAM = "inputstream";
    private final String OUTPUT_STREAM = "outputstream";
    private final String TIME = "time";
    private final String EDIT = "editMode";
    private final String X = "x";
    private final String Y = "y";
    private final String ID = "id";
    private final String DISTANCE = "distance";
    private final String VELOCITY = "velocity";
    private final String DIRECTION = "direction";
    private final String BLOB = "blob";
    private final String ID1 = "id1";
    private final String ID2 = "id2";
    private final String ID3 = "id3";
    private final String ID4 = "id4";
    private final String ID5 = "id5";
    private final String ID6 = "id6";

    private String mFileName;
    private String mName = "";
    private Document mDoc;
    private Element mSession;
    private boolean mSessionStarted = false;

    private StringBuilder mInputStream;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS", Locale.UK);

    public static LogManager getInstance()
    {
        if(mInstance == null)
            mInstance = new LogManager();
        return mInstance;
    }

    protected LogManager() {}

    public void startSession()
    {
        if(mSessionStarted) return;

        // dir
        File dir = new File(Environment.getExternalStorageDirectory() + "/Android/data/android.brailler++.logs");
        //File dir = new File("/sdcard/testes/brailler++.logs");
        dir.mkdirs();

        File file = null;
        do
        {
            // generate unique file name
            Time t = new Time();
            t.setToNow();
            mName =  t.toMillis(false) + ".xml";

            mFileName = dir + "/" + mName;
            file = new File(mFileName);
        }
        while(file.exists());

        // initialize input stream
        mInputStream = new StringBuilder();
        mInputStream.setLength(0);

        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // create doc element
            mDoc = db.newDocument();

            // create session element and attributes
            mSession = mDoc.createElement(SESSION);
            mSession.setAttribute(TIME, dateFormat.format(new java.util.Date()).toString());
            mSession.setAttribute(INPUT_STREAM, mInputStream.toString());
            mDoc.appendChild(mSession);

            mSessionStarted = true;

            Log.v("Brailler++", "log session started");
        }
        catch(Exception e)
        {
            Log.v("Brailler++", "ERROR: error in starting log session");
            e.printStackTrace();
        }
    }

    public String getFileName()
    {
        return mName;
    }

    public String getFilePath()
    {
        return mFileName;
    }

    public String getInputStream()
    {
        return mInputStream.toString();
    }


    public void logCharEvent(String character, Date d, Boolean isEdit)
    {
        if(!mSessionStarted) return;
        // create char element
        Element c = mDoc.createElement(CHAR);
        c.setTextContent(character.toString());

        // set time
        c.setAttribute(TIME, dateFormat.format(d));

        //set if edit is active
        c.setAttribute(EDIT, isEdit.toString());

        mSession.appendChild(c);

        // add character to input stream
        mInputStream.append(character);
    }


    public String endSession(String output)
    {
        if(!mSessionStarted) return "";
        try
        {
            mSession.setAttribute(INPUT_STREAM, mInputStream.toString());
            mSession.setAttribute(OUTPUT_STREAM, output.toString());

            DOMSource source = new DOMSource(mDoc);

            File file = new File(mFileName);
            Result result = new StreamResult(file);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
            mSessionStarted = false;
            Log.v("Brailler++", "log ended");
            return mName;

        }catch(Exception e)
        {
            Log.v("Brailler++", "ERROR: error in endind log session");
            e.printStackTrace();
            return "";
        }
    }

}