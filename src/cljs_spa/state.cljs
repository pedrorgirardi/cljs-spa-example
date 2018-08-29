(ns cljs-spa.state
  (:require [reagent.core :as r]))

(defonce !state (r/atom nil))

