package placemate.placemate;

import android.graphics.Bitmap;

/**
 * Created by scottmccracken on 08/05/2017.
 */

public class PlaceData {
    private String name;
    private int rating;
    private String website;
    private String phone;
    private String description;
    private Bitmap placeImage;

    public PlaceData(String name, String phone){
        this.name = name;
        this.phone = phone;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }

    public void setPhone(String phone){
        this.name = phone;
    }
    public String getPhone(){
        return this.phone;
    }

    public void setWebsite(String website){
        this.website = website;
    }
    public String getWebsite(){
        return this.website;
    }

    public void setDescription(String description){
        this.description = description;
    }
    public String getDescription(){
        return this.description;
    }

    public void setRating(int rating){
        this.rating = rating;
    }
    public int getRating(){
        return this.rating;
    }

    public void setImage(Bitmap placeImage){
        this.placeImage = placeImage;
    }
    public Bitmap getImage(){
        return this.placeImage;
    }

}
