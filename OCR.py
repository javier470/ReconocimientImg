from flask import Flask, request, jsonify
import pytesseract
from PIL import Image
from datetime import datetime
import io
import pyodbc

app = Flask(__name__)

connection_string = (
    "DRIVER={ODBC Driver 17 for SQL Server};"
    "SERVER=DESKTOP-6Q5B4LN;"
    "DATABASE=ocrDataPython;"
    "UID=root;"
    "PWD=root;"
)


def save_to_database(name, ocr_text, size, upload_date):
    try:
        conn = pyodbc.connect(connection_string)
        cursor = conn.cursor()
        
        sql = "INSERT INTO document (name, ocr_text, size, upload_date) VALUES (?, ?, ?, ?)"
        cursor.execute(sql, (name, ocr_text, size, upload_date))
        conn.commit()
        
        print("Datos guardados exitosamente.")
    except Exception as e:
        print("Error al guardar en la base de datos:", e)
    finally:
        cursor.close()
        conn.close()

@app.route("/", methods=['GET'])
def index():
    return jsonify({'Response': 'test'})

@app.route("/getOCR", methods=['POST'])
def get_OCR_data():
    file = request.files['file']

    if file:
        img = Image.open(io.BytesIO(file.read()))
        ocr_text = pytesseract.image_to_string(img)

        name = file.filename
        size = file.content_length
        upload_date = datetime.now()
        save_to_database(name, ocr_text, size, upload_date)

        return jsonify({'Response': ocr_text})
    else:
        return jsonify({'Response': 'No file found.'})

if __name__ == '__main__':
    app.run(port=5000)   
