package com.baremaps.workflow;

import java.util.List;

public record Step(String id, List<String> needs, Task task) {

}
