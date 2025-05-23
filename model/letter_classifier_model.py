# This file is a letter classifier that uses a preexisting
# model architecture that will be trained to identify ASL letters

# Code was written based of this video tutorial: https://www.youtube.com/watch?v=tHL5STNJKag
# @author Matthew Talle

import torch.nn as nn # imports specific functions related to neural networks
import timm # library that contains prepackaged model architectures for image classification


class LetterClassifier(nn.Module): # Subclassing Module from neural networks library

    """ Constructor """
    def __init__(self, num_classes=29): # number of possible image classifications
        super(LetterClassifier, self).__init__() # parent constructor

        # constructs the model using a pre-existing model architecture that has pre-trained weights
        # EfficientNet-B0 is a foundational architecture in the EfficientNet family of convolutional neural
        # networks (CNNs) designed for image classification tasks.
        self.base_model = timm.create_model('efficientnet_b0', pretrained=True)
        self.features = nn.Sequential(*list(self.base_model.children())[:-1])

        enet_out_size = 1280
        # Make a classifier
        self.classifier = nn.Sequential(
            nn.Flatten(),
            nn.Linear(enet_out_size, num_classes)
        )

    """ Takes in an image or a batch of images and returns the output of the classifier """
    def forward(self, x):
        # Connect these parts and return the output
        # model(image) =====>  model.forward(image) -> model.classifier(model.features(image)) -> output tensor
        x = self.features(x)
        output = self.classifier(x)
        return output