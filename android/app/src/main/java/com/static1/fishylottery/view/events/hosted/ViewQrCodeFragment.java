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
/**
 * Fragment that displays a QR code for a hosted event.
 *
 * <p>The QR code encodes a deep-link URL containing the event ID, allowing
 * attendees to quickly navigate to the event details by scanning it.</p>
 */

public class ViewQrCodeFragment extends Fragment {
    private Event event;
    /**
     * Inflates the QR code layout, retrieves the event argument, and generates
     * a QR bitmap to display.
     *
     * <p>If an event with a valid ID is provided, a deep-link URL is built and
     * converted into a QR image via {@link QrUtils}. The resulting bitmap is
     * then shown in the image view.</p>
     *
     * @return the root view for the QR code screen
     */
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
