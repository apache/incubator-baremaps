package com.baremaps.pipeline;

import java.util.List;

public interface Step {

  String id();

  List<String> needs();

  void execute(Context context);

}
