package com.lostcoding.savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            // use this setting to improve performance if you know taht the changes
            // in the content do not change the layout size of the recyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify a viewAdapter
            adapter = viewAdapter
        }
    }

    class MyAdapter(private val myDataset: Array<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        /**
         * Provide a reference to the views for each data item
         * complex data items may need more than one view per item
         * you provide access to all the views for a data item in a view holder
         * each data item is just a string in this case that is shown in a textview
         */
        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.ViewHolder {
            // create a new view
            val textView = LayoutInflater.from(parent.context).inflate(R.layout.my_text_view,parent, false) as TextView
            // set the view's size, margins, paddings and layout parameters
            textView.textSize = 16F
            textView.height = 100
            textView.width = 200

            return ViewHolder(textView)
        }

        // Replace the contents of a view (invoked by the layout manager)

    }
}
