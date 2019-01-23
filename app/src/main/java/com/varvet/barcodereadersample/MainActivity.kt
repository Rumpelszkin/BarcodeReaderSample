package com.varvet.barcodereadersample

import android.Manifest
import android.app.Activity
import android.support.v4.app.FragmentManager
import android.content.Context
import android.content.*
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.varvet.barcodereadersample.barcode.BarcodeCaptureActivity
import org.json.JSONObject
import javax.sql.CommonDataSource
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat
import com.varvet.barcodereadersample.utils.CipherClass
import com.varvet.barcodereadersample.utils.PathUtil.getPath
import com.varvet.barcodereadersample.utils.toJson
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import java.io.*
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.E
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private val PREFS_CONTACTS = "prefs_contacts"
    private val KEY_CONTACTS = "contacts_list"
    private val ADD_CONTACT_REQUEST = 2
    private val GET_FILE_URI_REQUEST = 531

    private var key = " ";


    private lateinit var myContactsListView: ListView
    private lateinit var listViewAdapter: ArrayAdapter<String>


    val storeFileName = "securedStore";

    val keyPrefix = "vss";
    //it's better to provide one, and you need to provide the same key each time after the first time
    val seedKey: ByteArray = "SecuredSeedData".toByteArray();

    companion object {
        const val QR_CODE_KEY = "pwr_krol"
        private val LOG_TAG = MainActivity::class.java.simpleName
        private val BARCODE_READER_REQUEST_CODE = 1
        private val ADD_NEW_CONTACT = 2
        public var tryb = 0
    }

 //   public lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) run {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        //getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE).edit().clear().commit()
        SecuredPreferenceStore.init(getApplicationContext(), storeFileName, keyPrefix, seedKey, DefaultRecoveryHandler())
        val prefStore = SecuredPreferenceStore.getSharedInstance()


        //prefStore.edit().clear().commit()

     //   mContext = getContext()
        myContactsListView = findViewById(R.id.myContactsListView)

        myContactsListView.setOnItemClickListener { parent, view, position, id ->


            val xd = prefStore.getString(listViewAdapter.getItem(position), null)

            // Toast.makeText(this, listViewAdapter.getItem(position) + " " + xd,Toast.LENGTH_SHORT).show()
            val fm = fragmentManager
            val dialogFragment = CipherMenuDialog()
            var args = Bundle()
            args!!.putString("name", listViewAdapter.getItem(position))
            args!!.putString("key", xd)


            dialogFragment.setArguments(args)
            dialogFragment.show(fm, "CIPHER_FRAGMENT")

        }

      myContactsListView.setOnItemLongClickListener { parent, view, position, id ->


          val builder = AlertDialog.Builder(this@MainActivity)

          // Set the alert dialog title
          builder.setTitle("Usunięcie kontaktu")

          // Display a message on alert dialog
          builder.setMessage("Czy chcesz usunąć wybrany kontakt ?")

          // Set a positive button and its click listener on alert dialog
          builder.setPositiveButton("Tak"){dialog, which ->
              // Do something when user press the positive button
              Toast.makeText(applicationContext,"Kontakt został usunięty.",Toast.LENGTH_LONG).show()


              val xd = prefStore.getString(listViewAdapter.getItem(position), null)

              // Toast.makeText(this, listViewAdapter.getItem(position) + " " + xd,Toast.LENGTH_SHORT).show()
              var string = this.getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE).getString("string", "[]")//dostep do SH
              var contactsList = getArrayListFromJson(string)
              if(contactsList != null) {
                  var adapter = ContactAdapter(contactsList)
                  adapter.removeItem(listViewAdapter.getItem(position))
              }
              Log.e("test","pjona")
              prefStore.edit().remove(listViewAdapter.getItem(position)).commit()

              if (contactsList != null) {
                  var adapter = ContactAdapter(contactsList)
              }


              listViewAdapter = ArrayAdapter<String>(this, R.layout.row, contactsList)
              myContactsListView.adapter = listViewAdapter



            //  finish()
            //  startActivity(intent)



          }


          // Display a negative button on alert dialog
          builder.setNegativeButton("Nie"){dialog,which ->
              Toast.makeText(applicationContext,"Nie usunięto kontaktu.",Toast.LENGTH_LONG).show()
          }


      /*    // Display a neutral button on alert dialog
          builder.setNeutralButton("Anuluj"){_,_ ->
              Toast.makeText(applicationContext,"Wybór został anulowany.",Toast.LENGTH_LONG).show()
          }
*/
          // Finally, make the alert dialog using builder
          val dialog: AlertDialog = builder.create()

          // Display the alert dialog on app interface
          dialog.show()


          true
        }





        val string = this.getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE).getString("string", "[]")
        var contactsList = getArrayListFromJson(string)

        if (contactsList != null) {
            var adapter = ContactAdapter(contactsList)
        }


        listViewAdapter = ArrayAdapter<String>(this, R.layout.row, contactsList)
        myContactsListView.adapter = listViewAdapter


        findViewById<Button>(R.id.button).setOnClickListener {
            showMenuDialog()

        }
    }

    override fun onRestart() {
        super.onRestart()
        if(tryb == 1 || tryb == 2){
       // Toast.makeText(applicationContext,"Operacja zakończona POWODZENIEM.",Toast.LENGTH_LONG).show();
        tryb =0}
        else if(tryb == -1){
            Toast.makeText(applicationContext,"Operacja zakończona NIEPOWODZENIEM. Niepoprawny rozmiar pliku.",Toast.LENGTH_LONG).show();
            tryb =0
        }
    }

    override fun onResume() {
        super.onResume()
        val string = this.getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE).getString("string", "[]")
        var contactsList = getArrayListFromJson(string)

        if (contactsList != null) {
            var adapter = ContactAdapter(contactsList)
        }


        listViewAdapter = ArrayAdapter<String>(this, R.layout.row, contactsList)
        myContactsListView.adapter = listViewAdapter


    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun getArrayListFromJson(Json: String): ArrayList<String>? {
        if (Json.equals("{}")) return null
        val listType = TypeToken.getParameterized(ArrayList::class.java, String::class.java).type
        val x: ArrayList<String> = Gson().fromJson(Json, listType)
        return x
    }

    private fun showMenuDialog() {
        val pop = MenuDialogFragment()
        val fm = fragmentManager
        pop.show(fm, "name")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    val barcode = data.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
                    val p = barcode.cornerPoints


                    val intent = Intent(applicationContext, AddScanedContact::class.java)

                    intent.putExtra("newName", barcode.displayValue.toString());
                    startActivityForResult(intent, ADD_CONTACT_REQUEST)
                }
            } else
                Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)))
        } else if (requestCode == ADD_CONTACT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    var string = this.getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE).getString("string", "[]")//dostep do SH
                    var contactsList = getArrayListFromJson(string)


                    if (contactsList != null) {
                        var adapter = ContactAdapter(contactsList)
                        val str = data.extras.getString("messenger")
                        val ou = str.split("<...>")

                        if(adapter.itemExists(ou[0])) {

                            var x = Math.random() * 100
                        while(adapter.itemExists(ou[0]+ x.roundToInt().toString())){
                            var x = Math.random() * 1000
                        }

                            adapter.addItem(ou[0]+x.roundToInt().toString())
                            val prefStore = SecuredPreferenceStore.getSharedInstance()
                            prefStore.edit().putString(ou[0]+x.roundToInt().toString(), ou[1]).apply()
                            Toast.makeText(getApplicationContext(),"Dodany kontakt zawierał powtarzającą się nazwę, w celu zachowania ich unikalności dodano unikalny numer. Dodano nowy kontakt: " + ou[0]+x.roundToInt().toString()  ,Toast.LENGTH_LONG).show();
                        }else{
                            adapter.addItem(ou[0])
                            val prefStore = SecuredPreferenceStore.getSharedInstance()
                            prefStore.edit().putString(ou[0], ou[1]).apply()
                            Toast.makeText(getApplicationContext(),"Dodano kontakt: " + ou[0] ,Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }


        } else
            if (requestCode == GET_FILE_URI_REQUEST && resultCode == Activity.RESULT_OK) {
                data?.data?.also { uri: Uri ->
                    //uri// to jest uri
                    val file = File(uri.getPath()) //create path from uri
                    Log.d("testGetUriString", uri.toString())//fileFromUri.toString());
                    val split = uri.toString().split(":") //split the path.
                    val filePath = split[1];//assign it to a string(your choice).
                    Log.d("testEloPathELO", filePath)//fileFromUri.toString());
                    var paczka = intent.extras
                    var klucz = key//paczka.getString("key")
                    Log.e("test",Integer.toString(tryb))
//                    var tryb = paczka.getInt("tryb")

                   if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) run {

                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    } else {

                        var CipherClass = CipherClass(uri, applicationContext, klucz, tryb)//edit
                   }


                    readUri(uri)
                    Log.d("elo", "elo2")
                }
            } else
                super.onActivityResult(requestCode, resultCode, data)


    }

    fun getUri(key : String,  tryb: Int) {// cipher = 1 // decipher = 2
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)


        intent.type = "text/*"
        //intent.type = "*/*"
        this.key = key;
        Companion.tryb = tryb
        intent.putExtra("key",key)
        intent.putExtra("tryb" , tryb)
        startActivityForResult(intent, GET_FILE_URI_REQUEST)
    }

    fun readUri(uri: Uri) {
        var filename: String
        val mimeType = getContentResolver().getType(uri);
        if (mimeType == null) {
            val path = getPath(this, uri);
            if (path == null) {
                filename = (uri.toString());
            } else {
                val file = File(path);
                filename = file.getName();
            }
        } else {
            val returnUri = uri
            val returnCursor = getContentResolver().query(returnUri, null, null, null, null);
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            filename = returnCursor.getString(nameIndex);
//            val size = Long.toString(returnCursor.getLong(sizeIndex));
        }
       // val fileSave = getExternalFilesDir(null);
        val sourcePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
       // val sourcePath = getExternalFilesDir(null).toString(); /// ----TUTAAAJ 1


       // val sourcePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
        try {
            Log.d("testtest", sourcePath)
      //      var tekstDoOdszyfrowania = copyFileStream(File(sourcePath + "/xxx"+ filename), uri, this);

        } catch (e: java.lang.Exception) {
            e.printStackTrace();
        }
    }


    private fun copyFileStream(dest: File, uri: Uri, context: Context): ByteArray? {
        var `is`: InputStream? = null
   //     var os: OutputStream? = null
        var bajtaraj : ByteArray? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
          //  os = FileOutputStream(dest)
            val buffer = ByteArray(1024)

            bajtaraj = ByteArray(`is`.available())
           // var length: Int = `is`!!.read(buffer)
           // var length: Int =
            `is`?.read(bajtaraj)

/*
            val sb = StringBuilder()
            while ((length) > 0) {

                sb.append(String(buffer))
                //os!!.write(buffer, 0, length)
               // os!!.write(bajtaraj, 0, length)
                length = `is`!!.read(bajtaraj)
                //length = `is`!!.read(buffer)
            }*/
           // os.write(bajtaraj)

            Log.d("testtest",String(bajtaraj))//sb.toString())// bajtaraj to cały tekst
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            `is`!!.close()
         //   os!!.close()
            return bajtaraj
        }
    }

  /*  public fun getContext(): Context{
        return mContext;
    }*/

    private fun makeAdapter(list: List<String>): ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, list)

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }

    inner class ContactAdapter(private val contactsList: ArrayList<String>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val row = layoutInflater.inflate(R.layout.layout_main, parent, false)

            return row
        }

        fun addItem(name: String) {
            contactsList.add(name)
            var sh = getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE)
            sh.edit().putString("string", contactsList.toJson()).apply()
        }

        fun removeItem(name: String){
            contactsList.remove(name)
            var sh = getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE)
            sh.edit().putString("string", contactsList.toJson()).apply()
        }

        fun itemExists(name:String): Boolean{
            return contactsList.contains(name)
        }
        override fun getItem(position: Int): Any {
            return contactsList.get(position) //To change body of created functions use File | Settings | File Templates.
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return contactsList.size //To change body of created functions use File | Settings | File Templates.
        }


    }

}



