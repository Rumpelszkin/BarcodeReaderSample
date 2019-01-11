package com.varvet.barcodereadersample

import android.app.Activity
import android.app.FragmentManager
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
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.support.v4.app.ActivityCompat.startActivityForResult
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


class MainActivity : AppCompatActivity() {

    private val PREFS_CONTACTS = "prefs_contacts"
    private val KEY_CONTACTS = "contacts_list"
    private val ADD_CONTACT_REQUEST = 2
    private val GET_FILE_URI_REQUEST = 531

    private var key = " ";
    private var tryb = 1;

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //getSharedPreferences(KEY_CONTACTS, Context.MODE_PRIVATE).edit().clear().commit()
        SecuredPreferenceStore.init(getApplicationContext(), storeFileName, keyPrefix, seedKey, DefaultRecoveryHandler());
        val prefStore = SecuredPreferenceStore.getSharedInstance()
        myContactsListView = findViewById(R.id.myContactsListView)

        myContactsListView.setOnItemClickListener { parent, view, position, id ->


            val xd = prefStore.getString(listViewAdapter.getItem(position), null)

            // Toast.makeText(this, listViewAdapter.getItem(position) + " " + xd,Toast.LENGTH_SHORT).show()
            val fm = fragmentManager
            val dialogFragment = CipherMenuDialog()
            var args = Bundle()
            args!!.putString("name", listViewAdapter.getItem(position))
            args!!.putString("key", xd)
           // args!!.putString("")
            dialogFragment.setArguments(args)
            dialogFragment.show(fm, "CIPHER_FRAGMENT")

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
                        adapter.addItem(ou[0])

                        val prefStore = SecuredPreferenceStore.getSharedInstance()
                        prefStore.edit().putString(ou[0], ou[1]).apply()

                    }

                }
            }


        } else
            if (requestCode == GET_FILE_URI_REQUEST && resultCode == Activity.RESULT_OK) {
                data?.data?.also { uri: Uri ->
                    //uri// to jest uri
                    val file = File(uri.getPath()) //create path from uri
                    Log.d("elo123", uri.toString())//fileFromUri.toString());
                    val split = uri.toString().split(":") //split the path.
                    val filePath = split[1];//assign it to a string(your choice).
                    Log.d("elo", filePath)//fileFromUri.toString());
                    var paczka = intent.extras
                    var klucz = key//paczka.getString("key")
                    Log.e("test",Integer.toString(tryb))
//                    var tryb = paczka.getInt("tryb")

                    var CipherClass = CipherClass(uri, applicationContext, klucz, tryb)//edit



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
        this.key = key;
        this.tryb = tryb;
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
        //val sourcePath = "/data/user/0/Downloads/rmp" //getFilesDir().toString();
        val sourcePath = getExternalFilesDir(null).toString();
       // val sourcePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
        try {
            Log.d("testtest", sourcePath)
            var tekstDoOdszyfrowania = copyFileStream(File(sourcePath + "/xxx"+ filename), uri, this);

        } catch (e: java.lang.Exception) {
            e.printStackTrace();
        }
    }


    private fun copyFileStream(dest: File, uri: Uri, context: Context): ByteArray? {
        var `is`: InputStream? = null
        var os: OutputStream? = null
        var bajtaraj : ByteArray? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
            os = FileOutputStream(dest)
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
            os!!.close()
            return bajtaraj
        }
    }

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



