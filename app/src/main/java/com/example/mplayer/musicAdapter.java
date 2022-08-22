package com.example.mplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class musicAdapter extends ArrayAdapter<musicLibrary> {
    Context context;
    ArrayList<musicLibrary> arrayList;
    public musicAdapter(@NonNull Context context, ArrayList<musicLibrary> arrayList) {

        super(context, 0,arrayList);
        this.context=context;
        this.arrayList=arrayList;
    }
    public void update(ArrayList<musicLibrary> result)
    {
        arrayList.clear();
       // arrayList=new ArrayList<>();
        arrayList.addAll(result);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
       View view=convertView;
       if(view==null){
           view= LayoutInflater.from(context).inflate(R.layout.list_view_item,parent,false);
       }
       musicLibrary musixLibrary=getItem(position);
        TextView musixName=view.findViewById(R.id.txtSong);
        musixName.setText(musixLibrary.getTitle().trim());
        ImageView imageView=view.findViewById(R.id.musixImage);
        byte[] image=getAlbumArt(musixLibrary.getPath());
        if(image!=null)
        {
            Glide.with(context).asBitmap().load(image).into(imageView);
        }
        else {
            Glide.with(context).load(R.drawable.music).into(imageView);
        }
        return view;
    }
    private  byte[] getAlbumArt(String uri)
    {
        byte[] art = new byte[0];
        try {

            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            retriever.setDataSource(uri);
            art=retriever.getEmbeddedPicture();
            retriever.release();

        }catch (Exception e){

        }
        return art;
    }
}
