(ns cljs-spa.page.profile
  (:require [cljs-spa.util :as util]
            [goog.object :as gobj]
            [re-frame.core :as rf]))

(defn activate [{{:keys [id]} :params}]
  (-> (util/safe-fetch (str "https://reqres.in/api/users/" id "?page=1"))
      (.then (fn [r] (.json r)))
      (.then (fn [js-data]
               (rf/dispatch [:cljs-spa.page.users/!user-data (js->clj js-data :keywordize-keys true)])
               (rf/dispatch [:!page-state :loaded])))))

(defn page-ui [{:keys [id]}]
  [:div
   [:h3 "User No. " id]
   (when-let [user-data @(rf/subscribe [:cljs-spa.page.users/?user-data])]
     [:div
      [:img {:src (-> user-data :data :avatar)}]
      (-> user-data :data :first_name)])])
