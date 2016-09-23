package co.thnki.brandfever;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.adapters.ProductImagesAdapter;

public class ProductUploadActivity extends AppCompatActivity
{
    public static final String PRODUCT_IMAGES = "productImages";

    @Bind(R.id.productRecyclerView)
    RecyclerView mProductRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_upload);
        ButterKnife.bind(this);

        ArrayList<String> productImages = getIntent()
                .getBundleExtra(PRODUCT_IMAGES)
                .getStringArrayList(PRODUCT_IMAGES);
        mProductRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        ProductImagesAdapter adapter = new ProductImagesAdapter(productImages);
        mProductRecyclerView.setAdapter(adapter);
    }

    /*

    Cursor cursor = getActivity().getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mImagesEncodedList.add(cursor.getString(columnIndex));
                    cursor.close();

    */
}
