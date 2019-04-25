package com.makeuproulette.android;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gtomato.android.ui.widget.CarouselView;

public class CarouselAdapter extends CarouselView.Adapter<CarouselAdapter.ViewHolder> {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.carousel_layout, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.blackCircle.setImageResource(R.drawable.blackcircle);
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public static class ViewHolder extends CarouselView.ViewHolder {

        public ImageView blackCircle;

        public ViewHolder(View view) {
            super(view);
            blackCircle = view.findViewById(R.id.blackCircle);
        }

    }



}
