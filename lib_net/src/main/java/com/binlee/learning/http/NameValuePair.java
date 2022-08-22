package com.binlee.learning.http;

import java.io.Serializable;

/**
 * Created on 18-9-19.
 *
 * @author leebin
 */
public interface NameValuePair extends Serializable {

  String getName();

  String getValue();
}
