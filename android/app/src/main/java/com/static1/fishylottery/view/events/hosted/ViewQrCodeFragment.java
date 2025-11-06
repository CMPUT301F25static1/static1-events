package com.static1.fishylottery.view.events.hosted;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.services.QrUtils;

public class ViewQrCodeFragment extends Fragment {
    private Event event;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_qr_code, container, false);
        ImageView imageViewQrCode = view.findViewById(R.id.image_qr_code);

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        if (event != null && event.getEventId() != null) {
            String eventQrCode = "com.static1.fishylottery://events?id=" + event.getEventId();
            Bitmap bitmap = QrUtils.generateQrCode(eventQrCode, 512);

            if (bitmap != null) {
                imageViewQrCode.setImageBitmap(bitmap);
            }
        }

        return view;
    }
}
