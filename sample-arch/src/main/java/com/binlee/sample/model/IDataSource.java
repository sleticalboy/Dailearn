package com.binlee.sample.model;

import java.util.List;

/**
 * Created on 21-2-26.
 *
 * @author binlee sleticalboy@gmail.com
 */
public interface IDataSource {

  <T> T query(Class<T> clazz, String selection, String[] args);

  <T> List<T> queryAll(Class<T> clazz);

  <T> void update(T obj);

  <T> void updateBatch(List<T> list);

  <T> void delete(T obj);

  <T> void deleteBatch(List<T> list);
}
