package com.udacity.catpoint.image.service.service;

import java.awt.image.BufferedImage;

public interface ImageService {

    boolean imageContainsCat(BufferedImage image, float confidenceThreshhold);


}
