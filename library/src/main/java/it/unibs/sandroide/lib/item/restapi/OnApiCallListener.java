package it.unibs.sandroide.lib.item.restapi;

/**
 * Created by giova on 14/03/2017.
 */

public interface OnApiCallListener {
    void onResult(int statusCode, String body);
}
