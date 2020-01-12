package io.eberlein.contacts.viewholders;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.InvocationTargetException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.eberlein.contacts.R;
import io.eberlein.contacts.interfaces.VHInterface;


public class VH<T> extends RecyclerView.ViewHolder implements VHInterface {
    T object;
    private boolean extraMenuOpen = false;

    @BindView(R.id.btn_delete) Button delete;
    @BindView(R.id.btn_edit) Button edit;
    @BindView(R.id.btn_one) Button one;
    @BindView(R.id.btn_two) Button two;
    @BindView(R.id.tv_left_up) TextView left_up;
    @BindView(R.id.tv_left_middle) TextView left_middle;
    @BindView(R.id.tv_left_bottom) TextView left_bottom;
    @BindView(R.id.tv_right_up) TextView right_up;
    @BindView(R.id.tv_right_middle) TextView right_middle;
    @BindView(R.id.tv_right_bottom) TextView right_bottom;

    public void onSelected(){

    }

    public void onEdit(){

    }

    public void onDelete(){

    }

    @OnClick
    void onClick(){
        if(!extraMenuOpen) onSelected();
        else closeExtraMenu();
    }

    @OnLongClick
    void onLongClick(){
        if(extraMenuOpen) closeExtraMenu();
        else openExtraMenu();
    }

    @OnClick(R.id.btn_edit)
    void onBtnEditClicked(){
        onEdit();
    }

    @OnClick(R.id.btn_delete)
    void onBtnDeleteClicked(){
        onDelete();
    }

    private void openExtraMenu(){
        extraMenuOpen = true;
        one.setVisibility(View.GONE);
        two.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        edit.setVisibility(View.VISIBLE);
        right_up.setVisibility(View.GONE);
        right_middle.setVisibility(View.GONE);
        right_bottom.setVisibility(View.GONE);
    }

    private void closeExtraMenu(){
        extraMenuOpen = false;
        one.setVisibility(View.VISIBLE);
        two.setVisibility(View.VISIBLE);
        delete.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);
        right_up.setVisibility(View.VISIBLE);
        right_middle.setVisibility(View.VISIBLE);
        right_bottom.setVisibility(View.VISIBLE);
    }

    public void onBind(){ }

    public void onSetObject(){ }

    public VH(View v){
        super(v);
        ButterKnife.bind(this, v);
        onBind();
    }

    public void setObject(T object){
        this.object = object;
        onSetObject();
    }

    public static <T, V extends VH<T>> V create(Class<V> cls, View v){
        try {
            return cls.getConstructor(View.class).newInstance(v);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e){
            e.printStackTrace();
            return null;
        }
    }
}
