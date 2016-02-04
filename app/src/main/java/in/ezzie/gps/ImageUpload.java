package in.ezzie.gps;
//482436188607117 app id

import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import in.ezzie.gps.app.Config;
import in.ezzie.gps.helper.PrefManager;
import in.ezzie.gps.helper.responseMessage;


public class ImageUpload extends AppCompatActivity {
    protected static final String TAG = "ImageUpload";
    protected static final int REQUEST_CAMERA = 1, SELECT_FILE = 2;

    private PrefManager pref;
    private ImageView viewImage;
    private Button btnCapture;
    private Bitmap image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

        btnCapture=(Button)findViewById(R.id.btnSelectPhoto);
        viewImage=(ImageView)findViewById(R.id.viewImage);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        pref = new PrefManager(getApplicationContext());
        String base64 = pref.getImage();
        if(base64 != null) {
            image = base64ToBitmap(base64);
            viewImage.setImageBitmap(image);
        }
    }



    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ImageUpload.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("mobile",pref.getMobileNumber());
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                viewImage.setImageBitmap(thumbnail);
                params.add("image", bitmapToBase64(thumbnail));
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);
                viewImage.setImageBitmap(bm);
                params.add("image", bitmapToBase64(bm));
            }
            new ImageUploadTask(params).execute();
        }
    }



        // ***************************************
        // Private methods
        // ***************************************

    private class ImageUploadTask extends AsyncTask<Void, Void, responseMessage> {
        private MultiValueMap<String, String> params;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        public ImageUploadTask(MultiValueMap<String, String> params){
            this.params = params;
        }
        @Override
        protected responseMessage doInBackground(Void... voids) {
          return Config.sendData(Config.URL_IMAGE_UPLOAD,this.params,TAG);
        }

        @Override
        protected void onPostExecute(responseMessage response) {
            super.onPostExecute(response);
            if(!response.getError()){
                pref.setImage(String.valueOf(this.params.get("image")));
            }
            Toast.makeText(ImageUpload.this,response.getMessage(),Toast.LENGTH_LONG).show();
        }

//        private String bitmapToBase64(Bitmap bitmap) {
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//            byte[] byteArray = byteArrayOutputStream .toByteArray();
//            return Base64.encodeToString(byteArray, Base64.DEFAULT);
//        }
    }


    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }



}
