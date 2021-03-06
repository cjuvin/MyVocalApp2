package com.example.carla.myvocalapplication;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.carla.myvocalapplication.MainActivity.REQUEST_CODE_NO_LISTENING;
import static com.example.carla.myvocalapplication.MainActivity.REQUEST_CODE_Shoplist;
import static com.example.carla.myvocalapplication.MainActivity.REQ_CODE;



public class Shoplist extends AppCompatActivity  implements VoiceManager.VoiceManagerListener{
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private ListView mVoiceInputTv;
    private String LIST="list.txt";
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

        private Item (String name, Boolean strike)
        {
            this.name = name;
            this.Strike=strike;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoplist);


        mVoiceInputTv =  findViewById(R.id.voiceInput);
        _data = new ArrayList<>();
        adaptater = new MyListAdaptater(this,R.layout.list, _data);

        ImageButton mSpeakBtn = findViewById(R.id.btnSpeak);

        voiceManager = new VoiceManager(this);
        voiceManager.setVoiceManagerListener(this);
        voiceManager.speak(REQUEST_CODE_Shoplist, "Say I need or add, to add an item to your shopping list or say read or what's in my list to read the list.");





        mSpeakBtn.setOnClickListener(v -> startVoiceInput());
    }



    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What item do you want to add?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Log.i("",a.getMessage());
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
            StringBuilder lu = new StringBuilder();
            while ((value = input.read()) != -1) {
                lu.append((char) value);
            }




            String[] strRead = lu.toString().split(",");

                for (String s : strRead){
                    String[] item = s.split(":");
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

        toSpeech= new TextToSpeech(this, status -> {
            if (status==TextToSpeech.SUCCESS)
            {
                toSpeech.setLanguage(Locale.UK);
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

        @SuppressLint("ViewHolder")
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            @SuppressLint("ViewHolder") View v;
            v = inflater.inflate(layout, parent, false);

            String str = l.get(position).name;
            ImageButton delete =  v.findViewById(R.id.buttonDelete);
            TextView item =  v.findViewById(R.id.text);
            item.setText(str);
            if (l.get(position).Strike)
                item.getPaint().setStrikeThruText(true);

            delete.setOnClickListener(v1 -> {
                l.remove(position);
                mVoiceInputTv.setAdapter(adaptater);
            });
            item.setOnClickListener(v12 -> {
                l.get(position).Strike=!l.get(position).Strike;
                mVoiceInputTv.setAdapter(adaptater);

            });
            return v;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            FileOutputStream out = openFileOutput(LIST, MODE_PRIVATE);
            StringBuilder str= new StringBuilder() ;
            for (Item i : _data
                 ) {
                str.append(i.name).append(":").append(i.Strike.toString()).append(",");

            }
            out.write(str.toString().getBytes());
            out.close();
        }catch (FileNotFoundException e ) {e.printStackTrace();} catch (IOException e ){e.printStackTrace();}
        _data.clear();

    }
}
