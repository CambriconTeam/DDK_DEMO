cmake_minimum_required(VERSION 2.8)

if(ANDROID)
  add_library(caffe_jni SHARED caffe_jni.cpp caffe_mobile.cpp)
  target_link_libraries(caffe_jni ${Caffe_LINK})
  caffe_default_properties(caffe_jni)

  # set back RUNTIME_OUTPUT_DIRECTORY
  set_target_properties(caffe_jni PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${PROJECT_BINARY_DIR}/android")

  caffe_set_solution_folder(caffe_jni android)

  # install
  install(TARGETS caffe_jni DESTINATION lib)
  #huangyaling

  add_library(detecte_jni SHARED detecte_jni.cpp caffe_detection.cpp)
  target_link_libraries(detecte_jni ${Caffe_LINK})
  caffe_default_properties(detecte_jni)

  # set back RUNTIME_OUTPUT_DIRECTORY
  set_target_properties(detecte_jni PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${PROJECT_BINARY_DIR}/android")
  caffe_set_solution_folder(detecte_jni android)

  # install
  install(TARGETS detecte_jni DESTINATION lib)
  #huangyaling

  add_library(face_jni SHARED face_jni.cpp face_detector.cpp)
  #add_executable(face_jni face_jni.cpp face_detector.cpp)
  target_link_libraries(face_jni ${Caffe_LINK} ${OpenCV_LIBS})
  caffe_default_properties(face_jni)

  # set back RUNTIME_OUTPUT_DIRECTORY
  set_target_properties(face_jni PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${PROJECT_BINARY_DIR}/android")

  caffe_set_solution_folder(face_jni android)

  # install
  install(TARGETS face_jni DESTINATION lib)
  #Chengyu Yang

  add_library(single_jni SHARED single_jni.cpp caffe_mobile.cpp)
  target_link_libraries(single_jni ${Caffe_LINK} ${OpenCV_LIBS})
  caffe_default_properties(single_jni)

  # set back RUNTIME_OUTPUT_DIRECTORY
  set_target_properties(single_jni PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${PROJECT_BINARY_DIR}/android")

  caffe_set_solution_folder(single_jni android)

  # install
  install(TARGETS single_jni DESTINATION lib)
  #Chengyu Yang

endif()
