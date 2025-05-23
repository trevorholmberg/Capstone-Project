# This file acts as the dataset architecture to be used when creating the dataset that the model will use
# Code was written based of this video tutorial: https://www.youtube.com/watch?v=tHL5STNJKag
# @author Matthew Talle


# parent class to extend when creating a dataset architecture involes
# mapping keys (ex. A, B...) to data samples (A.jpg, B.jpg)
from torch.utils.data import Dataset

# image folder class that takes a dataset directory and automatically
# assumes that all the subdirectories is its own class for the images inside them,
# uses each subdirectory's name as the class name, and maps each class name to
# an integer label starting from 0 and then map all the images into to its corresponding label
from torchvision.datasets import ImageFolder



class ASLDataset(Dataset): # subclassing Dataset from torch library

    """ Constructor that takes in the file path to the image dataset, and a transform that is applied to each item
    in the dataset and resize it to the same size"""
    def __init__(self, data_dir, transform=None):
        self.data = ImageFolder(data_dir, transform)

    """ tells how many images we have in the dataset """
    def __len__(self):
        return len(self.data)

    """ returns a tuple containing image tensor and its corresponding label (0 - num of subdirectories in dataset) """
    def __getitem__(self, idx):
        return self.data[idx]

    """ returns all data classes from image folder """
    @property
    def classes(self):
        return self.data.classes

# dataset.class_to_idx returns {'classname':0 , ... } could use in the future