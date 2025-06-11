import pickle
import json
import streamlit as st
import tensorflow as tf
import sklearn as sk
import pandas as pd
import datetime as dt

# loading data
m1 = {} # linear regression
m1data = {}

m2 = {} # neural network
m2_8 = {}
m2data = {}
m2_8data = {}
m2scalerX = {}
m2scalerY = {}

# neural networks
m2_8 = {}
m2data_8 = {}

with open("models/m1.json", "r") as f:
    m1data = json.load(f)
with open("models/m2.json", "r") as f:
    m2data = json.load(f)
with open("models/m2_8.json", "r") as f:
    m2_8data = json.load(f)

with open("models/m1.bin", "rb") as f:
    m1 = pickle.load(f)

m2 = tf.keras.models.load_model("models/m2.keras")
m2_8 = tf.keras.models.load_model("models/m2_8.keras")

with open("models/m2scalerX.bin", "rb") as f:
    m2scalerX = pickle.load(f)
with open("models/m2scalerY.bin", "rb") as f:
    m2scalerY = pickle.load(f)

m2scalerX = m2scalerX.set_output(transform="pandas")
m2scalerY = m2scalerY.set_output(transform="pandas")
# data loaded



def predict(df):
    df_trim = df[["hour_digit", "clouds_all"]]
    df_poly = sk.preprocessing.PolynomialFeatures(degree=m1data["polynominal_degree"], include_bias=True).fit_transform(df_trim)
    m1res = m1.predict(df_poly)

    df_scaled      = m2scalerX.transform(df)
    df_scaled_trim = df_scaled[["hour_digit", "clouds_all"]]

    m2resNorm = m2.predict(df_scaled_trim)
    m2res     = m2scalerY.inverse_transform(m2resNorm)

    m2_8resNorm = m2_8.predict(df_scaled)
    m2_8res = m2scalerY.inverse_transform(m2_8resNorm)

    return [m1res[0][0], m2res[0][0], m2_8res[0][0]]

st.header("Параметры", divider=True)

left, right  = st.columns(2)

weather_main_map = {
    0: "Ясно",
    1: "Облачно",
    2: "Осадки",
    3: "Катаклизмы"
}

precipitation_type_map = {
    0: "Дождь",
    1: "Снег"
}

months = [
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
]

with left:
    st.header("Время")
    default_time = dt.time(8, 0)
    hour_input = st.time_input("Время", default_time, step=dt.timedelta(hours = 1))
    month_input = st.selectbox("Месяц", months)
    st.divider()
    holiday_input = st.toggle("Праздник")

with right:
    st.header("Погода")
    temp_input = st.slider("Температура", 244, 310, value=281)
    clouds_input  = st.slider("Облачность", 0, 100, value=50)
    st.divider()
    weather_main_input = st.segmented_control("Опишите погоду", weather_main_map.keys(),
                                             format_func=lambda option: weather_main_map[option],
                                             selection_mode="single", default=0)
    st.divider()
    precipitation_type_input = right.segmented_control("Тип осадков", precipitation_type_map.keys(),
                                                        format_func=lambda option: precipitation_type_map[option],
                                                        selection_mode="single", default=0)
    precipitation_input = right.slider("Количество осадков", 0, 10_000, value=0)

submit_button = st.button("Подтвердить")

if submit_button:
    precipitation_type_is_rain = not precipitation_type_input or precipitation_type_map[precipitation_type_input] == "rain"

    df  = pd.DataFrame({'temp': temp_input,
                        'snow_1h':          [0 if precipitation_type_is_rain else precipitation_input],
                        'clouds_all':       [0 if clouds_input is None else int(clouds_input)],
                        'weather_main':     [0 if weather_main_input is None else weather_main_input],
                        'rain_1h':          [0 if not precipitation_type_is_rain else precipitation_input],
                        'holiday_digit':    [0 if holiday_input is None else int(holiday_input)],
                        'month_digit':      [0 if month_input is None else months.index(month_input)],
                        'hour_digit':       [0 if hour_input is None else hour_input.hour],
                        })
    st.header("Введенные данные")
    st.write(df)
    predicted = predict(df)

    data = {
        "Модель": ["m1", "m2", "m2_8"],
        "Предсказанное значение": [predicted[0], predicted[1], predicted[2]],
        "RMSE": [m1data["rmse"], m2data["rmse"], m2_8data["rmse"]],
        "R2": [m1data["r2"], m2data["r2"], m2_8data["r2"]],
    }

    df = pd.DataFrame(data)

    st.header("Результат")
    st.table(df)
