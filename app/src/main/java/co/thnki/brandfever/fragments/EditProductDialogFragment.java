package co.thnki.brandfever.fragments;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.R;
import co.thnki.brandfever.firebase.database.models.ProductBundle;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.interfaces.Const;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.ConnectivityUtil;

import static co.thnki.brandfever.Brandfever.toast;

public class EditProductDialogFragment extends DialogFragment implements Const
{
    private Products mProduct;

    @Bind(R.id.priceInputText)
    EditText mPriceAfter;

    @Bind(R.id.discountInputText)
    EditText mPriceBefore;

    @Bind(R.id.brandInputText)
    EditText mBrandName;

    @Bind(R.id.xsInputText)
    EditText mXsCount;

    @Bind(R.id.sInputText)
    EditText mSCount;

    @Bind(R.id.mInputText)
    EditText mMCount;

    @Bind(R.id.lInputText)
    EditText mLCount;

    @Bind(R.id.xlInputText)
    EditText mXlCount;

    @Bind(R.id.xxlInputText)
    EditText mXxlCount;

    @Bind(R.id.scrollView)
    ScrollView mScrollView;

    @Bind(R.id.scrollViewChild)
    LinearLayout mScrollViewChild;
    private Map<String, Integer> mSizesMap;

    public EditProductDialogFragment()
    {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View parentView = inflater.inflate(R.layout.fragment_edit_product, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        mBrandName.setText(mProduct.getBrand());
        mPriceAfter.setText(mProduct.getPriceAfter());
        mPriceBefore.setText(mProduct.getPriceBefore());

        mSizesMap = mProduct.getSizesMap();

        mXsCount.setText(mSizesMap.get(getString(R.string.xs)) + "");
        mSCount.setText(mSizesMap.get(getString(R.string.s)) + "");
        mMCount.setText(mSizesMap.get(getString(R.string.m)) + "");
        mLCount.setText(mSizesMap.get(getString(R.string.l)) + "");
        mXlCount.setText(mSizesMap.get(getString(R.string.xl)) + "");
        mXxlCount.setText(mSizesMap.get(getString(R.string.xxl)) + "");
        return parentView;
    }

    @OnClick(R.id.closeDialog)
    public void close()
    {
        dismiss();
    }

    @OnClick(R.id.saveProduct)
    public void save()
    {
        /**
         * get all the text box values
         * validate it
         * put the values in mProduct
         * setValue for given key to server
         */
        if (ConnectivityUtil.isConnected())
        {
            int value = getValidatedNum(mPriceAfter, false);
            if (value > 0)
            {
                mProduct.setPriceAfter(value + "");
                value = getValidatedNum(mPriceBefore, false);
                if (value > 0)
                {
                    mProduct.setPriceBefore(value + "");
                    String text = mBrandName.getText().toString().trim();
                    if (text.isEmpty())
                    {
                        toast(R.string.pleaseEnterValidBrandName);
                        scrollToRow(mBrandName);
                    }
                    else
                    {
                        mProduct.setBrand(text);
                        value = getValidatedNum(mXsCount, true);
                        if (value >= 0)
                        {
                            mSizesMap.put(getString(R.string.xs), value);
                            value = getValidatedNum(mSCount, true);
                            if (value >= 0)
                            {
                                mSizesMap.put(getString(R.string.s), value);
                                value = getValidatedNum(mMCount, true);
                                if (value >= 0)
                                {
                                    mSizesMap.put(getString(R.string.m), value);
                                    value = getValidatedNum(mLCount, true);
                                    if (value >= 0)
                                    {
                                        mSizesMap.put(getString(R.string.l), value);
                                        value = getValidatedNum(mXlCount, true);
                                        if (value >= 0)
                                        {
                                            mSizesMap.put(getString(R.string.xl), value);
                                            value = getValidatedNum(mXxlCount, true);
                                            if (value >= 0)
                                            {
                                                mSizesMap.put(getString(R.string.xxl), value);
                                                mProduct.setSizesMap(mSizesMap);
                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                                        .child(mProduct.getCategoryId())
                                                        .child(mProduct.getProductId());
                                                reference.setValue(mProduct);
                                                mProduct.getProductId();
                                                dismiss();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    private int getValidatedNum(EditText editText, boolean isSize)
    {
        Editable editable = editText.getText();
        if (editable != null)
        {
            String temp = editable.toString();
            if (!temp.isEmpty())
            {
                try
                {
                    temp = temp.replace('\u20B9' + "", "");
                    return Integer.parseInt(temp);
                }
                catch (NumberFormatException e)
                {
                    toast(R.string.pleaseEnterAWholeNumber);
                    scrollToRow(editText);
                    return -3;
                }
            }
            if (isSize)
            {
                toast(R.string.pleaseEnterAValidNumber);
            }
            else
            {
                toast(R.string.pleaseEnterAValidPrice);
            }
            scrollToRow(editText);
            return -2;
        }
        toast(R.string.please_try_again);
        scrollToRow(editText);
        return -1;
    }

    private void scrollToRow(final View viewToShow)
    {
        viewToShow.requestFocus();
        Rect rect = new Rect();
        viewToShow.getHitRect(rect);
        mScrollView.requestChildRectangleOnScreen(mScrollViewChild, rect, false);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.post(mProduct);
        Otto.unregister(this);
    }

    public static EditProductDialogFragment getInstance(ProductBundle productBundle)
    {
        EditProductDialogFragment fragment = new EditProductDialogFragment();
        fragment.mProduct = new Products(productBundle);
        return fragment;
    }

}
