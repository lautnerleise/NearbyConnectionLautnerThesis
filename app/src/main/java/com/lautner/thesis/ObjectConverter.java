package com.lautner.thesis;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectConverter
{
    public static byte[] serialize(Object object) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(object);
            os.flush();
            os.close();
            Log.d("serializedObject", out.toByteArray().toString());
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("serialize EXCEPTION", e.toString());
            return null;
        }
    }

    public static Object deserialize(byte[] data){
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return is.readObject();
        }
        catch (IOException e){
            e.printStackTrace();
            Log.d("deserialize EXCEPTION 1", e.toString());
            return null;
        }
        catch (ClassNotFoundException e){
            e.printStackTrace();
            Log.d("deserialize EXCEPTION 2", e.toString());
            return null;
        }
    }
}
