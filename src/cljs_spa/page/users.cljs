(ns cljs-spa.page.users
  (:require [re-frame.core :as rf]
            [cljs-spa.util :as util]))

(defn activate []
  (-> (util/safe-fetch "https://reqres.in/api/users?page=1")
      (.then (fn [r] (.json r)))
      (.then (fn [js-data]
               (rf/dispatch [::!users-data (js->clj js-data :keywordize-keys true)])
               (rf/dispatch [:!page-state :loaded])))))

(defn page-ui []
  [:div
   [:h3 "Users"]
   (when-let [users-data @(rf/subscribe [::?users-data])]
     (->> users-data
          :data
          (map (fn [user]
                 [:li [:a {:href (str "#/users/" (:id user))} (:first_name user)]]))
          (into [:ul])))])


(rf/reg-event-db ::!users-data
  (fn [db [_ users-data]]
    (assoc-in db [:page/users :users-data] users-data)))

(rf/reg-sub ::?users-data
  (fn [db _]
    (get-in db [:page/users :users-data])))

(rf/reg-event-db ::!user-data
  (fn [db [_ user-data]]
    (assoc-in db [:page/users :user-data] user-data)))

(rf/reg-sub ::?user-data
  (fn [db _]
    (get-in db [:page/users :user-data])))
