package de.kai_morich.simple_usb_terminal;

import android.view.View;
import android.widget.AdapterView;

import java.util.Objects;

final class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
    interface SelectionHandler {
        void onSelected(int position);
    }

    private final SelectionHandler handler;

    SimpleItemSelectedListener(SelectionHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        handler.onSelected(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No action. Both settings spinners always keep one entry selected.
    }
}
