# This program was made to test the model and the predictions it makes
# @author Matthew Talle


import torch
import os
from PIL import Image
import torchvision.transforms as transforms
from letter_classifier_model import LetterClassifier

# instantiate the model class and load its saved state post training
model = LetterClassifier()
model.load_state_dict(torch.load('letter_classifier.pth', weights_only=True))

# function that resizes all passed in images to same size and converts them to input tensors
transform = transforms.Compose([
    transforms.Resize((128, 128)), # TODO: Needs to be 224x224 for future training
    transforms.ToTensor()])

directory = 'to_test/'

""" Obtaining image from directory and preprocessing it to be used for input"""
def preprocess_image(image_name):
    image_path = os.path.join(r"C:\Users\crist\OneDrive\Desktop\WCU CS\capstone-project-fall-2024-495-01-a-mobile_app_matthew_t_trevor_h_matthew_a\model\to_test", image_name)
    image = Image.open(image_path)
    image = transform(image).unsqueeze(0)  # Apply the same transform as used during training
    return image

""" feeds the model an image and returns the output of the model"""
def predict_index(model, image_name):
    model.eval()  # Make sure the model is in evaluation mode
    image = preprocess_image(image_name)
    output = model(image)
    _, predicted = torch.max(output, 1)  # Get the index of the highest log-probability
    return predicted.item()

""" Converts the label the model predicts to have the highest probability into ascii char """
def predict_letter(index):
    if index < 26:
        return chr(index + 65) # ascii conversion
    elif index == 26:
        return 'delete'
    elif index == 27:
        return 'empty'
    elif index == 28:
        return 'space'

""" Entry point to program where we iterate through a directory of images and predict the letter """
def main():
    for filename in os.listdir(directory):
        predicted_index = predict_index(model, filename)
        predicted_letter = predict_letter(predicted_index)
        print(filename + "\n\tTensor Index : " + str(predicted_index) + "\n\tPredicted Letter : " + str(predicted_letter) + "\n\n")

main()