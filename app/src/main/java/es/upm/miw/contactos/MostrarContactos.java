package es.upm.miw.contactos;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

import static android.provider.ContactsContract.Contacts;

public class MostrarContactos extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> contactos;
    private final String LOG_TAG = "MiW"; // Etiqueta para filtrar logs

    Context context;

    private static final int REQUEST_RUNTIME_PERMISSION = 123;
    String[] permissons = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_mostrar_contactos);


        if (CheckPermission(MostrarContactos.this, permissons[0])) {
            // you have permission go ahead
            listarContactos();


        } else {
            // you do not have permission go request runtime permissions
            RequestPermission(MostrarContactos.this, permissons, REQUEST_RUNTIME_PERMISSION);
        }



    }

    public boolean CheckPermission(Context context, String Permission) {
        if (ContextCompat.checkSelfPermission(context,
                Permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {

            case REQUEST_RUNTIME_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // you have permission go ahead
                    listarContactos();
                } else {
                    // you do not have permission show toast.
                    Toast toast=Toast.makeText(getApplicationContext(),"No tienes permiso para acceder a los contactos", Toast.LENGTH_SHORT);
                    toast.setMargin(50,50);
                    toast.show();
                }
                return;
            }
        }
    }

    public void RequestPermission(Activity thisActivity, String[] Permission, int Code) {
        if (ContextCompat.checkSelfPermission(thisActivity,
                Permission[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Permission[0])) {
            } else {
                ActivityCompat.requestPermissions(thisActivity, Permission,
                        Code);
            }
        }
    }



    protected void listarContactos(){

        listView = (ListView) findViewById(R.id.listadoContactos);

        // cargar lista de contactos
        //contactos = cargarDatos();
        contactos = cargarDatosDetalle();

        // crear adaptador y enchufarlo al listView
        ArrayAdapter adaptador = new ArrayAdapter<String>(
                this,
                R.layout.layout_contacto,
                contactos
        );
        listView.setAdapter(adaptador);
    }

    /**
     * Crea una lista con los nombres de los contactos
     * @return Lista de contactos
     */
    public ArrayList<String> cargarDatos() {
        ArrayList<String> listaContactos = new ArrayList<>();

        // Obtener un cursor al proveedor de contactos y recuperar todos
        Uri URI_Contactos = Contacts.CONTENT_URI;  // "content://com.android.contacts/contacts"
        Log.i(LOG_TAG, "CONTENT_URI=" + URI_Contactos.toString());

        // Obtener content resolver y recuperar contactos
        ContentResolver cr = getContentResolver();
        String ordenacion = Contacts.DISPLAY_NAME_PRIMARY + " ASC";     // display_name ASC


        Cursor cursor = cr.query(URI_Contactos, null, null, null, ordenacion);
        Log.i(LOG_TAG, "Número contactos=" + Integer.toString(cursor.getCount()));

        // Si hay datos -> cargar en la lista
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String namePrimary =
                        cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY));

                listaContactos.add(namePrimary);
                Log.i(LOG_TAG, "Contacto: " + namePrimary);
                cursor.moveToNext();
            }
            cursor.close();  // liberar recursos
        }

        return listaContactos;
    }


    /**
     * Crea una lista con los nombres de los contactos
     * @return Lista de contactos
     */
    public ArrayList<String> cargarDatosDetalle() {

        ArrayList<String> emlRecs = new ArrayList<String>();
        HashSet<String> emlRecsHS = new HashSet<String>();

        ContentResolver cr = getContentResolver();

        String[] PROJECTION = new String[] {
                ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID };


        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";

        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";

        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
        if (cur.moveToFirst()) {
            do {
                // names comes in hand sometimes
                String name = cur.getString(1);
                String emlAddr = cur.getString(3);

                // keep unique only
                if (emlRecsHS.add(emlAddr.toLowerCase())) {
                    emlRecs.add(name + " | " + emlAddr);
                }
            } while (cur.moveToNext());
        }

        cur.close();


        return emlRecs;
    }

}
