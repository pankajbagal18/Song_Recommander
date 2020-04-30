package com.example.songrecommander;

public class SongData {
    private String a_name;
    private String title;
    private String spot_id;
    private double tempo;
    private double energy;
    private double danceability;
    private double loudness;
    private double valence;
    private double acousticness;
    private double mood;
    private String mood_info;

    public SongData()
    {

    }
    public SongData(String a_name, String title, String spot_id, double tempo, double energy, double danceability, double loudness, double valence, double acousticness, double mood, String mood_info) {
        this.a_name = a_name;
        this.title = title;
        this.spot_id = spot_id;
        this.tempo = tempo;
        this.energy = energy;
        this.danceability = danceability;
        this.loudness = loudness;
        this.valence = valence;
        this.acousticness = acousticness;
        this.mood = mood;
        this.mood_info = mood_info;
    }

    public String getA_name() {
        return a_name;
    }

    public void setA_name(String a_name) {
        this.a_name = a_name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpot_id() {
        return spot_id;
    }

    public void setSpot_id(String spot_id) {
        this.spot_id = spot_id;
    }

    public double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        this.tempo = tempo;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getDanceability() {
        return danceability;
    }

    public void setDanceability(double danceability) {
        this.danceability = danceability;
    }

    public double getLoudness() {
        return loudness;
    }

    public void setLoudness(double loudness) {
        this.loudness = loudness;
    }

    public double getValence() {
        return valence;
    }

    public void setValence(double valence) {
        this.valence = valence;
    }

    public double getAcousticness() {
        return acousticness;
    }

    public void setAcousticness(double acousticness) {
        this.acousticness = acousticness;
    }

    public double getMood() {
        return mood;
    }

    public void setMood(double mood) {
        this.mood = mood;
    }

    public String getMood_info() {
        return mood_info;
    }

    public void setMood_info(String mood_info) {
        this.mood_info = mood_info;
    }

    @Override
    public String toString() {
        return "SongData{" +
                "a_name='" + a_name + '\'' +
                ", title='" + title + '\'' +
                ", spot_id='" + spot_id + '\'' +
                ", tempo=" + tempo +
                ", energy=" + energy +
                ", danceability=" + danceability +
                ", loudness=" + loudness +
                ", valence=" + valence +
                ", acousticness=" + acousticness +
                ", mood=" + mood +
                ", mood_info='" + mood_info + '\'' +
                '}';
    }
}
