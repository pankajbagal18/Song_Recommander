package com.example.songrecommander;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.songrecommander.PlaylistAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowPlaylist extends AppCompatActivity {

    ListView songList;
    List<SongData> songDataList=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_playlist);
        songList = (ListView) findViewById(R.id.song_list);
        String mood_info = getIntent().getStringExtra("MOOD_INFO");
        songDataList = new ArrayList<SongData>();
        createPlaylist(mood_info);
    }

    private void createPlaylist(String mood)
    {
        final FirebaseDatabase songDatabase = FirebaseDatabase.getInstance();
        DatabaseReference songRef = songDatabase.getReference("song");
        songRef.orderByChild("mood_info").equalTo(mood).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {

                    for(DataSnapshot child:dataSnapshot.getChildren())
                    {
                        SongData songData = child.getValue(SongData.class);
                        songDataList.add(songData);
                        Log.d("Song",songData.toString());
                    }
                    PlaylistAdapter adapter = new PlaylistAdapter(ShowPlaylist.this, songDataList);
                    songList.setAdapter(adapter);
                }
                else
                    Log.d("createPlaylist","no record found");
                //Log.d("Song",dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Error happened"+databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}
