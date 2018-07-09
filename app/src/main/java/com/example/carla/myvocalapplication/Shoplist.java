package com.example.carla.myvocalapplication;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.carla.myvocalapplication.MainActivity.REQUEST_CODE_CHOOSE_ACTIVITY;
import static com.example.carla.myvocalapplication.MainActivity.REQUEST_CODE_CONTACT;
import static com.example.carla.myvocalapplication.MainActivity.REQUEST_CODE_NO_LISTENING;
import static com.example.carla.myvocalapplication.MainActivity.REQUEST_CODE_Shoplist;
import static com.example.carla.myvocalapplication.MainActivity.REQ_CODE;



public class Shoplist extends AppCompatActivity  implements VoiceManager.VoiceManagerListener{
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private ListView mVoiceInputTv;
    private ImageButton mSpeakBtn;
    private String LIST="list.txt";
    private FileOutputStream out=null;
    private FileInputStream in=null;
    private ArrayList<Item> _data;
    ArrayList<String> result;
    MyListAdaptater adaptater;
    TextToSpeech toSpeech;

    VoiceManager voiceManager;


    private class Item
    {
        String name;
        Boolean Strike;

        public Item (String name, Boolean strike)
        {
            this.name = name;
            this.Strike=strike;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoplist);


        mVoiceInputTv = (ListView) findViewById(R.id.voiceInput);
        _data = new ArrayList<Item>();
        adaptater = new MyListAdaptater(this,R.layout.list, _data);

        mSpeakBtn = (ImageButton) findViewById(R.id.btnSpeak);

        voiceManager = new VoiceManager(this);
        voiceManager.setVoiceManagerListener(this);
        voiceManager.speak(REQUEST_CODE_Shoplist, "Say I need or add, to add an item to your shopping list or say read or what's in my list to read the list.");





        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
    }



    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What item do you want to add?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    _data.add(new Item(result.get(0),false ));
                    mVoiceInputTv.setAdapter(adaptater);
                }
                break;
            }
        }
        voiceManager.speak(REQUEST_CODE_Shoplist, "Say I need or add, to add an item to your shopping list or say read or what's in my list to read the list. ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mVoiceInputTv.setAdapter(new MyListAdaptater(this,R.layout.list, _data));

        try {
            FileInputStream input = openFileInput(LIST);
            int value;
            // On utilise un StringBuffer pour construire la chaîne au fur et à mesure
            StringBuilder lu = new StringBuilder();
            // On lit les caractères les uns après les autres
            while ((value = input.read()) != -1) {
                // On écrit dans le fichier le caractère lu
                lu.append((char) value);
            }




            String[] strRead = lu.toString().split(",");

                for (int i = 0; i < strRead.length; i++) {
                    String[] item = strRead[i].split(":");
                    Item it  = new Item(item[0],Boolean.valueOf(item[1]));
                    _data.add(it);
                }

            if (in != null)
                in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        toSpeech= new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status==TextToSpeech.SUCCESS)
                {
                    toSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }

    @Override
    public void onResults(ArrayList<String> results, int type) {
        String _activity = VoiceManager.getString();

        if (_activity.contains("need") || _activity.contains("add")||_activity.contains("append "))
        {
            REQ_CODE = REQUEST_CODE_Shoplist;
            startVoiceInput();

        }
        else if (_activity.contains("what is")||_activity.contains("what's")|| _activity.contains("read"))
        {

            for (int i = 0; i<_data.size();i++
                 ) {
                toSpeech.speak(_data.get(i).name, TextToSpeech.QUEUE_ADD,null);
            }

            voiceManager.speak(REQUEST_CODE_Shoplist, "Say add or read ");

        }
        else

            voiceManager.speak(REQUEST_CODE_Shoplist, "Say add or read  ");

    }


    @Override
    public void onSpeechCompleted(int requestCode) {
        if (requestCode!=REQUEST_CODE_NO_LISTENING ) {
            Log.i("Shop", "OnSpeechCompleted");
            voiceManager.listen(requestCode);
        }

    }

    private  class MyListAdaptater extends ArrayAdapter<Item> {
        private int layout;
        List<Item> l;
        Context c;


        private MyListAdaptater(@NonNull Context context, int resource, @NonNull List<Item> objects) {
            super(context, resource, objects);
            layout = resource;
            this.c=context;
            this.l=objects;


        }

        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View v = inflater.inflate(layout, parent, false);

            String str = l.get(position).name;
            ImageButton delete = (ImageButton) v.findViewById(R.id.buttonDelete);
            TextView item =  v.findViewById(R.id.text);
            item.setText(str);
            if (l.get(position).Strike)
                item.getPaint().setStrikeThruText(true);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    l.remove(position);
                    mVoiceInputTv.setAdapter(adaptater);
                }
            });
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    l.get(position).Strike=!l.get(position).Strike;
                    mVoiceInputTv.setAdapter(adaptater);

                }
            });
            return v;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            out= openFileOutput(LIST, MODE_PRIVATE);
            String str = "";
            for (Item i : _data
                 ) {str = str + i.name +":"+ i.Strike.toString()+ ",";

            }
            out.write(str.getBytes());
            if (out != null)
                out.close();
        }catch (FileNotFoundException e ) {e.printStackTrace();} catch (IOException e ){e.printStackTrace();}
        _data.clear();

    }
}
