(ns cljs-spa.page.clock
  (:require [cljs-spa.util :as util]
            [re-frame.core :as rf]))

(defn tick []
  (js/console.log "Tick")

  (rf/dispatch [::!clock-time (int (/ (.valueOf (js/Date.)) 1000))]))

(defn activate []
  (tick)

  (rf/dispatch [::!clock (fn [interval]
                           (util/set-interval interval tick 1000))]))

(defn deactivate []
  (rf/dispatch [::!clock util/clear-interval]))

(defn page-ui []
  [:div
   [:h3 "Clock"]
   [:div "Seconds since epoch: " @(rf/subscribe [::?clock-time])]
   [:div
    "This pages demonstrates acquiring and disposing of resources. When the
user enters the page, a setInterval timer is created. While active, you can see
Tick messages in the console log. When the user navigates away (e.g. to #/home)
the timer is cleared."]])


(rf/reg-event-db ::!clock-time
  (fn [db [_ clock-time]]
    (assoc-in db [:page/clock :clock-time] clock-time)))

(rf/reg-sub ::?clock-time
  (fn [db _]
    (get-in db [:page/clock :clock-time])))

(rf/reg-event-db ::!clock
  (fn [db [_ f]]
    (update-in db [:page/home :clock] f)))