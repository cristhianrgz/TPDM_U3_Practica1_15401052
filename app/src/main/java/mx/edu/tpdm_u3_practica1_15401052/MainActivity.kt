package mx.edu.tpdm_u3_practica1_15401052

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    var descripcion : EditText ?= null
    var monto : EditText ?= null
    var fechaVen : EditText ?= null
    var pagado : EditText ?= null
    var listaView : ListView ?= null
    var insertar : Button ?= null
    var actualizar : Button ?= null

    //declarando el objeto firestore
    var baseRemota = FirebaseFirestore.getInstance()

    //declarar objetos tipo arreglo dinamico
    var registrosRemotos = ArrayList<String>()
    var keys = java.util.ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        descripcion = findViewById(R.id.editDescripcion)
        monto = findViewById(R.id.editMonto)
        fechaVen = findViewById(R.id.editfecha)
        pagado = findViewById(R.id.editpagado)
        listaView = findViewById(R.id.listaRegitrados)
        insertar = findViewById(R.id.btnInsertar)
        actualizar = findViewById(R.id.btnActualizar)

        insertar?.setOnClickListener {
            var insertarDatos = hashMapOf(
                "descripcion" to descripcion?.text.toString(),
                "monto" to monto?.text.toString().toDouble(),
                "fechaVencimiento" to fechaVen?.text.toString(),
                "pagado" to pagado?.text.toString()
            )
            baseRemota.collection("recibopagos").add(insertarDatos as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this,"Se inserto correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(this,"No se pudo insertar", Toast.LENGTH_SHORT).show()
                }
            limpiarCampos()
        }
        baseRemota.collection("recibopagos").addSnapshotListener { querySnapshot, e ->
            if(e != null){
                Toast.makeText(this,"Error, No se pudo realizar la consulta", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            registrosRemotos.clear()
            keys.clear()

            for (document in querySnapshot!!){
                var cadena = document.getString("descripcion")+" -- "+document.getDouble("monto")+" -- "+
                        document.getString("fechaVencimiento")+" -- "+document.getString("pagado")+"\n"
                registrosRemotos.add(cadena)
                keys.add(document.id)
            }

            if(registrosRemotos.size == 0){
                registrosRemotos.add("NO HAY REGISTROS PARA MOSTRAR")
            }

            var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, registrosRemotos)
            listaView?.adapter = adapter
        }
        listaView?.setOnItemClickListener { adapterView, view, i, l ->

        }
    }

    fun limpiarCampos(){
        descripcion?.setText("")
        monto?.setText("")
        fechaVen?.setText("")
        pagado?.setText("")
    }
}
